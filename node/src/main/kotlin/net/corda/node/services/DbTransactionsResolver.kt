package net.corda.node.services

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.internal.FetchMultiTransactionsFlow
import net.corda.core.internal.FetchTransactionsFlow
import net.corda.core.internal.ResolveTransactionsFlow
import net.corda.core.internal.TransactionsResolver
import net.corda.core.internal.dependencies
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.debug
import net.corda.core.utilities.seconds
import net.corda.node.services.api.WritableTransactionStorage
import net.corda.nodeapi.internal.persistence.contextTransaction
import sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl.ThreadStateMap.Byte0.suspended
import java.util.*

class DbTransactionsResolver(private val flow: ResolveTransactionsFlow) : TransactionsResolver {
    private var sortedDependencies: List<SecureHash>? = null
    private val logger = flow.logger

    @Suspendable
    override fun downloadDependencies(multiMode : Boolean) {
        logger.debug { "Downloading dependencies for transactions ${flow.txHashes}" }
        val transactionStorage = flow.serviceHub.validatedTransactions as WritableTransactionStorage

        // Maintain a work queue of all hashes to load/download, initialised with our starting set. Then do a breadth
        // first traversal across the dependency graph.
        //
        // TODO: This approach has two problems. Analyze and resolve them:
        //
        // (1) This flow leaks private data. If you download a transaction and then do NOT request a
        // dependency, it means you already have it, which in turn means you must have been involved with it before
        // somehow, either in the tx itself or in any following spend of it. If there were no following spends, then
        // your peer knows for sure that you were involved ... this is bad! The only obvious ways to fix this are
        // something like onion routing of requests, secure hardware, or both.
        //
        // (2) If the identity service changes the assumed identity of one of the public keys, it's possible
        // that the "tx in db is valid" invariant is violated if one of the contracts checks the identity! Should
        // the db contain the identities that were resolved when the transaction was first checked, or should we
        // accept this kind of change is possible? Most likely solution is for identity data to be an attachment.

        val nextRequests = LinkedHashSet<SecureHash>(flow.txHashes) // Keep things unique but ordered, for unit test stability.
        val topologicalSort = TopologicalSort()

        println("O/S DbTransactionsResolver.downloadDependencies(MultiMode=${multiMode})") //++++

/*++++ remove new
        val maxFetchBlockSize = 50 //++++ make parameter
        var batchInd: Int = 0//+++
        while (nextRequests.isNotEmpty()) {
            println("DbTransactionsResolver.downloadDependencies(): Main while (count=${nextRequests.size})")//++++
            // Don't re-download the same tx when we haven't verified it yet but it's referenced multiple times in the
            // graph we're traversing.
            nextRequests.removeAll(topologicalSort.transactionIds)
            if (nextRequests.isEmpty()) {
                // Done early.
                break
            }
//++++ comment below check actual platform version
            // In addition to fetchRequiredTransactions not working in batch mode when the platform version is below 7, as also
            // only want to fetch one at a time in this main loop to keep the footprint size down for checkpointing lest we
            // rollback the previous performance enhancement in this area (one at a time).
            val ind = batchInd++//++++
            println("\n\n[batch_ind=$ind] Fetch start++++ ************************************************************************************")//++++
            val requestItems = LinkedHashSet<SecureHash>()
            var index = 0
            while (nextRequests.size > 0 && requestItems.size < maxFetchBlockSize) {
                val item = nextRequests.first()
                nextRequests.remove(item)
                requestItems.add(item)
                val itemStr = item.toString()
                println("  ITEM[$index]: '$itemStr'")
                index++
            }

            // missingItems has been added to the return of fetchRequiredTransactions: This contains transactions that were not able
            // to be sent back due to the maximum message size (nominally 10MB) being breached. In this instance we re-request them.
            val (existingTxIds, downloadedTxs, missingItems) = fetchRequiredTransactions(requestItems) // Fetch a batch at a time
           //++++ val extSize = existingTxIds.size
           //++++ val downLdSz = downloadedTxs.size
            for (missing in missingItems) {
                println("Adding back missing item '${missing}'")
                nextRequests.add(missing)
            }

            println("[$ind] Fetch end++++ (existing_sz=${existingTxIds.size}, downloaded_sz=${downloadedTxs.size}) ************************************************************************************")//++++

            // When acquiring the write locks for the transaction chain, it is important that all required locks are acquired in the same
            // order when recording both verified and unverified transactions. In the verified case, the transactions must be recorded in
            // back chain order (i.e. oldest first), so this must also happen for unverified transactions. This sort ensures that locks are
            // acquired in the right order in the case the transactions should be stored in the database as unverified. The main topological
            // sort is also updated here to ensure that this contains everything that needs locking in cases where the resolver switches
            // from checkpointing to storing unverified transactions in the database.
          //++++ keep for enterprise:  val lockingSort = TopologicalSort()
            for (tx in downloadedTxs) {
                val dependencies = tx.dependencies
                //++++ keep for enterprise:      lockingSort.add(tx.id, dependencies)
                topologicalSort.add(tx.id, dependencies)
            }

            var suspended = true
            for (downloaded in downloadedTxs) {
                suspended = false
                val dependencies = downloaded.dependencies
                // Do not keep in memory as this bloats the checkpoint. Write each item to the database.
                //++++ keep for enterprise: transactionStorage.lockObjectsForWrite(lockingSort.complete(), contextTransaction, false) {
                transactionStorage.addUnverifiedTransaction(downloaded)
                //++++ keep for enterprise: }


                // The write locks are only released over a suspend, so need to keep track of whether the flow has been suspended to ensure
                // that locks are not held beyond each while loop iteration (as doing this would result in a deadlock due to claiming locks
                // in the wrong order)
                val suspendedViaAttachments = flow.fetchMissingAttachments(downloaded)
                val suspendedViaParams = flow.fetchMissingNetworkParameters(downloaded)
                suspended = suspended || suspendedViaAttachments || suspendedViaParams

                // Add all input states and reference input states to the work queue.
                nextRequests.addAll(dependencies)
            }

            // If the flow did not suspend on the last iteration of the downloaded loop above, perform a suspend here to ensure no write
            // locks are held going into the next while loop iteration.
            //++++ update comment above to the effect that the database is flushed..... (O/S)
            if (!suspended) {
                FlowLogic.sleep(0.seconds)
            }

            // It's possible that the node has a transaction in storage already. Dependencies should also be present for this transaction,
            // so just remove these IDs from the set of next requests.
            nextRequests.removeAll(existingTxIds)
        }
*/

// ++++ keep old
        while (nextRequests.isNotEmpty()) {
            println("DbTransactionsResolver.downloadDependencies(): Main while (count=${nextRequests.size})")//++++
            // Don't re-download the same tx when we haven't verified it yet but it's referenced multiple times in the
            // graph we're traversing.
            nextRequests.removeAll(topologicalSort.transactionIds)
            if (nextRequests.isEmpty()) {
                // Done early.
                break
            }

            // Request the standalone transaction data (which may refer to things we don't yet have).
            val (existingTxIds, downloadedTxs) = fetchRequiredTransactions(Collections.singleton(nextRequests.first())) // Fetch first item only
            for (tx in downloadedTxs) {
                val dependencies = tx.dependencies
                topologicalSort.add(tx.id, dependencies)
            }

            var suspended = true
            for (downloaded in downloadedTxs) {
                suspended = false
                val dependencies = downloaded.dependencies
                // Do not keep in memory as this bloats the checkpoint. Write each item to the database.
                transactionStorage.addUnverifiedTransaction(downloaded)

                // The write locks are only released over a suspend, so need to keep track of whether the flow has been suspended to ensure
                // that locks are not held beyond each while loop iteration (as doing this would result in a deadlock due to claiming locks
                // in the wrong order)
                val suspendedViaAttachments = flow.fetchMissingAttachments(downloaded)
                val suspendedViaParams = flow.fetchMissingNetworkParameters(downloaded)
                suspended = suspended || suspendedViaAttachments || suspendedViaParams

                // Add all input states and reference input states to the work queue.
                nextRequests.addAll(dependencies)
            }

            // If the flow did not suspend on the last iteration of the downloaded loop above, perform a suspend here to ensure that
            // all data is flushed to the database.
            if (!suspended) {
                FlowLogic.sleep(0.seconds)
            }

            // It's possible that the node has a transaction in storage already. Dependencies should also be present for this transaction,
            // so just remove these IDs from the set of next requests.
            nextRequests.removeAll(existingTxIds)
        }

        sortedDependencies = topologicalSort.complete()
        logger.debug { "Downloaded ${sortedDependencies?.size} dependencies from remote peer for transactions ${flow.txHashes}" }
    }

    override fun recordDependencies(usedStatesToRecord: StatesToRecord) {
        val sortedDependencies = checkNotNull(this.sortedDependencies)
        logger.debug { "Recording ${sortedDependencies.size} dependencies for ${flow.txHashes.size} transactions" }
        val transactionStorage = flow.serviceHub.validatedTransactions as WritableTransactionStorage
        for (txId in sortedDependencies) {
            // Retrieve and delete the transaction from the unverified store.
            val (tx, isVerified) = checkNotNull(transactionStorage.getTransactionInternal(txId)) {
                "Somehow the unverified transaction ($txId) that we stored previously is no longer there."
            }
            if (!isVerified) {
                tx.verify(flow.serviceHub)
                flow.serviceHub.recordTransactions(usedStatesToRecord, listOf(tx))
            } else {
                logger.debug { "No need to record $txId as it's already been verified" }
            }
        }
    }

    // The transactions already present in the database do not need to be checkpointed on every iteration of downloading
    // dependencies for other transactions, so strip these down to just the IDs here.
    /*++++ remove new
    @Suspendable
    private fun fetchRequiredTransactions(requests: Set<SecureHash>): Triple<List<SecureHash>, List<SignedTransaction>, List<SecureHash>> {
        val sz = requests.size//++++
        //++++val ost = flow.otherSide.javaClass.name
        //++++println("fetchRequiredTransactions X ++++ size=$sz $ost")
        var missingItems = ArrayList<SecureHash>() // Items that were not returned by a batch call
        if (requests.size == 1) {
            println("fetchRequiredTransactions (Single): size=$sz")//++++
            // We should probably put the test here for whether we're using multi fetch (batch size > 1 and same plat version).
            val requestedTxs = flow.subFlow(FetchTransactionsFlow(requests, flow.otherSide))
            return Triple(requestedTxs.fromDisk.map { it.id }, requestedTxs.downloaded, missingItems)
        } else {
            var byteCountRecv = 0
            var numRecv = 0
            println("fetchRequiredTransactions (Multiple): size=$sz")//++++
            val requestedTxs = flow.subFlow(FetchMultiTransactionsFlow(requests, flow.otherSide))
            var downloaded = ArrayList<SignedTransaction>()
            for (mTran in requestedTxs.downloaded) {
                if (mTran.isNull()) {
                    println("Failed to get transaction++++ ${mTran.id}")
                    missingItems.add(mTran.id)
                } else {
                    byteCountRecv += mTran.serializedByteCount()
                    downloaded.add(mTran.get())
                    numRecv++
                }
            }
            println("fetchRequiredTransactions (Multiple-end): Byte count=${byteCountRecv} (${numRecv} / ${requests.size})")//++++
            for (muld in downloaded) {
                println("MUL-DLD = '$muld.id': ${muld.javaClass.name}")
            }
            return Triple(requestedTxs.fromDisk.map { it.id }, downloaded, missingItems)
        }
//++++        println("fetchRequiredTransactions Y ++++ size=$sz")
//++++        return Pair(requestedTxs.fromDisk.map { it.id }, requestedTxs.downloaded)
    }

    //++++ */
//++++ keep old
    // The transactions already present in the database do not need to be checkpointed on every iteration of downloading
    // dependencies for other transactions, so strip these down to just the IDs here.
    @Suspendable
    private fun fetchRequiredTransactions(requests: Set<SecureHash>): Pair<List<SecureHash>, List<SignedTransaction>> {
        val requestedTxs = flow.subFlow(FetchTransactionsFlow(requests, flow.otherSide))
        return Pair(requestedTxs.fromDisk.map { it.id }, requestedTxs.downloaded)
    }


    /**
     * Provides a way to topologically sort SignedTransactions represented just their [SecureHash] IDs. This means that given any two transactions
     * T1 and T2 in the list returned by [complete] if T1 is a dependency of T2 then T1 will occur earlier than T2.
     */
    class TopologicalSort {
        private val forwardGraph = HashMap<SecureHash, MutableSet<SecureHash>>()
        val transactionIds = LinkedHashSet<SecureHash>()
        private val nonDupeHash = HashMap<SecureHash, SecureHash>()
        private fun dedupe(sh: SecureHash): SecureHash = nonDupeHash.getOrPut(sh) { sh }

        /**
         * Add a transaction to the to-be-sorted set of transactions.
         * @param txId The ID of the transaction.
         * @param dependentIds the IDs of all the transactions [txId] depends on.
         */
        fun add(txIdp: SecureHash, dependentIds: Set<SecureHash>) {
            val txId = dedupe(txIdp)
            require(transactionIds.add(txId)) { "Transaction ID $txId already seen" }
            dependentIds.forEach {
                // Note that we use a LinkedHashSet here to make the traversal deterministic (as long as the input list is).
                val deDupeIt = dedupe(it)
                forwardGraph.computeIfAbsent(deDupeIt) { LinkedHashSet() }.add(txId)
            }
        }

        /**
         * Return the sorted list of transaction IDs.
         */
        fun complete(): List<SecureHash> {
            val visited = HashSet<SecureHash>(transactionIds.size)
            val result = ArrayList<SecureHash>(transactionIds.size)

            fun visit(txId: SecureHash) {
                if (visited.add(txId)) {
                    forwardGraph[txId]?.forEach(::visit)
                    result += txId
                }
            }

            transactionIds.forEach(::visit)

            return result.apply(Collections::reverse)
        }
    }
}