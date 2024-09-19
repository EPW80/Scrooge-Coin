import java.util.ArrayList;

public class TxHandler {

    private UTXOPool utxoPool;

    /*
     * Creates a public ledger whose current UTXOPool (collection of unspent
     * transaction outputs) is utxoPool. This should make a defensive copy of
     * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // Defensive copy of UTXO pool
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /*
     * Returns true if
     * (1) all outputs claimed by tx are in the current UTXO pool,
     * (2) the signatures on each input of tx are valid,
     * (3) no UTXO is claimed multiple times by tx,
     * (4) all of tx’s output values are non-negative, and
     * (5) the sum of tx’s input values is greater than or equal to the sum of
     * its output values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        UTXOPool tempPool = new UTXOPool(); // Temporary pool to track used UTXOs
        double inputSum = 0, outputSum = 0;

        // Check (1) and (3): Ensure each input is valid and not reused
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

            // Check if the UTXO is in the pool
            if (!utxoPool.contains(utxo)) {
                return false; // Invalid: Output claimed by tx not in the pool
            }

            // Check for double-spending
            if (tempPool.contains(utxo)) {
                return false; // Invalid: Double spend detected
            }
            tempPool.addUTXO(utxo, utxoPool.getTxOutput(utxo));

            // Get the corresponding output to verify the signature
            Transaction.Output output = utxoPool.getTxOutput(utxo);
            RSAKey publicKey = (RSAKey) output.address; // This is the RSAKey from rsa.jar

            // Verify the signature using the correct RSAKey class
            byte[] rawData = tx.getRawDataToSign(i);
            if (!publicKey.verifySignature(rawData, input.signature)) {
                return false; // Invalid: Signature is not valid
            }

            inputSum += output.value; // Accumulate input values
        }

        // Check (4): Ensure all output values are non-negative
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) {
                return false; // Invalid: Negative output value
            }
            outputSum += output.value; // Accumulate output values
        }

        // Check (5): Ensure input sum is greater than or equal to output sum
        return inputSum >= outputSum;
    }

    /*
     * Handles each epoch by receiving an unordered array of proposed
     * transactions, checking each transaction for correctness,
     * returning a mutually valid array of accepted transactions,
     * and updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> validTxs = new ArrayList<>();

        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                validTxs.add(tx);

                // Remove spent outputs from the UTXO pool
                for (Transaction.Input input : tx.getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    utxoPool.removeUTXO(utxo);
                }

                // Add new outputs to the UTXO pool
                byte[] txHash = tx.getHash();
                for (int i = 0; i < tx.numOutputs(); i++) {
                    UTXO utxo = new UTXO(txHash, i);
                    utxoPool.addUTXO(utxo, tx.getOutput(i));
                }
            }
        }

        // Convert ArrayList to array
        Transaction[] result = new Transaction[validTxs.size()];
        return validTxs.toArray(result);
    }
}
