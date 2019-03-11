package nl.knokko.util.bits.test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;

import nl.knokko.util.bits.BitHelper;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitInputStream;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.bits.BooleanArrayBitInput;
import nl.knokko.util.bits.BooleanArrayBitOutput;
import nl.knokko.util.bits.ByteArrayBitInput;
import nl.knokko.util.bits.ByteArrayBitOutput;

public class BitTester {

	public static void main(String[] args) {
		testBooleanArrayBitOutput();
		testByteArrayBitOutput();
		
		testBytes();
	}
	
	static void testBytes() {
		for (byte b = Byte.MIN_VALUE; true; b++) {
			assert BitHelper.byteFromBinary(BitHelper.byteToBinary(b)) == b;
			boolean[] buffer = new boolean[12];
			BitHelper.byteToBinary(b, buffer);
			assert BitHelper.byteFromBinary(buffer, 0) == b;
			BitHelper.byteToBinary(b, buffer, 3);
			assert BitHelper.byteFromBinary(buffer, 3) == b;
			assert BitHelper.byteFromBinary(buffer[3], buffer[4], buffer[5], buffer[6], buffer[7], buffer[8],
					buffer[9], buffer[10]) == b;
			
			if (b == Byte.MAX_VALUE) {
				break;
			}
		}
	}
	
	static void testBooleanArrayBitOutput() {
		
		// Part 1: Test write/read correctness
		BooleanArrayBitOutput output = new BooleanArrayBitOutput(0);
		outputStuff(output);
		output.terminate();
		
		// Test the booleans
		boolean[] booleans = output.getBooleans();
		assert booleans.length == BIT_COUNT;
		assert Arrays.equals(booleans, output.getBackingArray());
		BitInput input = new BooleanArrayBitInput(booleans);
		checkInputStuff(input);
		input.terminate();
		
		// Test the bytes
		byte[] bytes = output.getBytes();
		assert bytes.length == BYTE_COUNT;
		input = new ByteArrayBitInput(bytes);
		checkInputStuff(input);
		input.terminate();
		
		// Also test BitInputStream now that we are at it anyway
		File test = new File("test.bin");
		try {
			if (test.exists()) {
				test.delete();
			}
			assert !test.exists();
			
			OutputStream out = Files.newOutputStream(test.toPath());
			out.write(bytes);
			out.flush();
			out.close();
			
			assert test.exists();
			input = new BitInputStream(Files.newInputStream(test.toPath()));
			checkInputStuff(input);
			input.terminate();
			
			test.delete();
			assert !test.exists();
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
		
		// Part 2: Test capacity behavior
		output = new BooleanArrayBitOutput(16);
		assert output.getBackingArray().length == 16;
		output.addDirectShort(Short.MIN_VALUE);
		assert output.getBackingArray().length == 16;
		output.ensureExtraCapacity(32);
		int capacity = output.getBackingArray().length;
		assert capacity >= 48;
		output.addInt(Integer.MAX_VALUE);
		assert output.getBackingArray().length == capacity;
		
		// Test if input also works if we challenge the capacity
		input = new BooleanArrayBitInput(output.getBackingArray());
		assert input.readShort() == Short.MIN_VALUE;
		assert input.readInt() == Integer.MAX_VALUE;
		input.terminate();
	}
	
	static void testByteArrayBitOutput() {
		
		// Part 1: Test write/read correctness
		ByteArrayBitOutput output = new ByteArrayBitOutput(0);
		outputStuff(output);
		output.terminate();
		
		// Test if correct bytes were produced
		byte[] bytes = output.getBytes();
		assert bytes.length == BYTE_COUNT;
		assert Arrays.equals(bytes, output.getBackingArray());
		BitInput input = new ByteArrayBitInput(bytes);
		checkInputStuff(input);
		input.terminate();
		
		// Test if correct booleans were produced
		boolean[] booleans = output.getBooleans();
		assert booleans.length == BIT_COUNT;
		input = new BooleanArrayBitInput(booleans);
		checkInputStuff(input);
		input.terminate();
		
		// Part 2: Test capacity behavior
		output = new ByteArrayBitOutput(4);
		assert output.getBackingArray().length == 4;
		output.addDirectInt(123456);
		assert output.getBackingArray().length == 4;
		output.ensureExtraCapacity(64);
		int capacity = output.getBackingArray().length;
		assert capacity >= 12;
		output.addLong(-123456789123456L);
		assert capacity == output.getBackingArray().length;
		output.terminate();
	}
	
	static final int BIT_COUNT = 638;
	static final int BYTE_COUNT = 80;
	
	static void outputStuff(BitOutput output) {
		output.addBooleans(false, true, true, false, true, false);
		output.addInt(394738457);
		output.addByte(Byte.MIN_VALUE);
		output.addShort(Short.MAX_VALUE);
		output.addChar('>');
		output.addBoolean(true);
		output.addJavaString(null);
		output.addChar(Character.MAX_VALUE);
		output.addNumber(-15, (byte) 5, true);
		output.addByte(Byte.MAX_VALUE);
		output.addString("Hello World!");
		output.addNumber(23456678, false);
		output.addShort(Short.MIN_VALUE);
		output.addJavaString("Hey Java...");
		output.addInt(Integer.MIN_VALUE);
		output.addLong(Long.MAX_VALUE);
		output.addNumber(7, (byte) 3, false);
		output.addString(null);
		output.addLong(Long.MIN_VALUE);
		output.addChar(Character.MIN_VALUE);
		output.addNumber(7, (byte) 4, true);
		output.addInt(Integer.MAX_VALUE);
	}
	
	static void checkInputStuff(BitInput input) {
		assert !input.readBoolean();
		assert input.readBoolean();
		assert input.readBoolean();
		assert !input.readBoolean();
		assert input.readBoolean();
		assert !input.readBoolean();
		assert input.readInt() == 394738457;
		assert input.readByte() == Byte.MIN_VALUE;
		assert input.readShort() == Short.MAX_VALUE;
		assert input.readChar() == '>';
		assert input.readBoolean();
		assert input.readJavaString() == null;
		assert input.readChar() == Character.MAX_VALUE;
		assert input.readNumber((byte) 5, true) == -15;
		assert input.readByte() == Byte.MAX_VALUE;
		assert input.readString().equals("Hello World!");
		assert input.readNumber(false) == 23456678;
		assert input.readShort() == Short.MIN_VALUE;
		assert input.readJavaString().equals("Hey Java...");
		assert input.readInt() == Integer.MIN_VALUE;
		assert input.readLong() == Long.MAX_VALUE;
		assert input.readNumber((byte) 3, false) == 7;
		assert input.readString() == null;
		assert input.readLong() == Long.MIN_VALUE;
		assert input.readChar() == Character.MIN_VALUE;
		assert input.readNumber((byte) 4, true) == 7;
		assert input.readInt() == Integer.MAX_VALUE;
	}
}