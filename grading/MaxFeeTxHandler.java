import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaxFeeTxHandler {
	private UTXOPool utxoPool;

	public MaxFeeTxHandler(UTXOPool utxoPool) {
		this.utxoPool = new UTXOPool(utxoPool);
	}

	public boolean isValidTx(Transaction tx) {
		Set<UTXO> usedUTXOs = new HashSet<>();
		double inputSum = 0, outputSum = 0;

		// Check for each input
		for (int i = 0; i < tx.numInputs(); i++) {
			Transaction.Input input = tx.getInput(i);
			UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

			if (!utxoPool.contains(utxo))
				return false; // UTXO doesn't exist in pool
			if (usedUTXOs.contains(utxo))
				return false; // Double spending

			// Verify signature using RSAKey's verifySignature method
			Transaction.Output output = utxoPool.getTxOutput(utxo);
			if (!output.address.verifySignature(tx.getRawDataToSign(i), input.signature)) {
				return false; // Invalid signature
			}

			usedUTXOs.add(utxo);
			inputSum += output.value;
		}

		// Check each output
		for (Transaction.Output output : tx.getOutputs()) {
			if (output.value < 0)
				return false; // Negative output value
			outputSum += output.value;
		}

		return inputSum >= outputSum; // Ensure input >= output
	}

	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		List<SortTx> validTxs = new ArrayList<>();

		for (Transaction tx : possibleTxs) {
			if (isValidTx(tx)) {
				SortTx txWithFee = new SortTx(tx, calculateFee(tx));
				validTxs.add(txWithFee);

				// Update UTXO pool
				processTransaction(tx);
			}
		}

		Collections.sort(validTxs); // Sort by fee
		Transaction[] result = new Transaction[validTxs.size()];
		for (int i = 0; i < validTxs.size(); i++) {
			result[i] = validTxs.get(validTxs.size() - i - 1).transaction;
		}
		return result;
	}

	private double calculateFee(Transaction tx) {
		double inputSum = 0, outputSum = 0;

		// Sum input values
		for (Transaction.Input input : tx.getInputs()) {
			UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
			inputSum += utxoPool.getTxOutput(utxo).value;
		}

		// Sum output values
		for (Transaction.Output output : tx.getOutputs()) {
			outputSum += output.value;
		}

		return inputSum - outputSum;
	}

	private void processTransaction(Transaction tx) {
		// Remove consumed UTXOs from the pool
		for (Transaction.Input input : tx.getInputs()) {
			UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
			utxoPool.removeUTXO(utxo);
		}

		// Add new UTXOs created by the transaction
		byte[] txHash = tx.getHash();
		for (int i = 0; i < tx.numOutputs(); i++) {
			UTXO utxo = new UTXO(txHash, i);
			utxoPool.addUTXO(utxo, tx.getOutput(i));
		}
	}

	class SortTx implements Comparable<SortTx> {
		public Transaction transaction;
		private double fee;

		public SortTx(Transaction tx, double fee) {
			this.transaction = tx;
			this.fee = fee;
		}

		@Override
		public int compareTo(SortTx other) {
			return Double.compare(other.fee, this.fee); // Higher fee comes first
		}
	}
}
