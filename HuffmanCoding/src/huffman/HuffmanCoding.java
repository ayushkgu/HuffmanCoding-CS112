package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * 
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) {
        fileName = f;
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by
     * frequency
     */
    public void makeSortedList() {
        /* Your code goes here */
        StdIn.setFile(fileName);
        int[] frequency = new int[128];
        int count = 0;
        ArrayList<CharFreq> arr = new ArrayList<>();

        for (int i = 0; i < frequency.length; i++) {
            frequency[i] = 0;
        }
        while (StdIn.hasNextChar() != false) {
            char c = StdIn.readChar();
            frequency[c]++;
            count++;
        }
        for (int j = 0; j < frequency.length; j++) {
            if (frequency[j] == 0) {
            } else {
                arr.add(new CharFreq((char) j, (double) frequency[j] / count));
            }
        }

        if (arr.size() == 1) {
            arr.add(new CharFreq((char) ((int) arr.get(0).getCharacter() + 1), 0));
        }

        sortedCharFreqList = arr;
        Collections.sort(sortedCharFreqList);
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
        /* Your code goes here */
        Queue<TreeNode> source = new Queue<TreeNode>();
        Queue<TreeNode> target = new Queue<TreeNode>();

        for (int i = 0; i < sortedCharFreqList.size(); i++) {
            source.enqueue(new TreeNode(sortedCharFreqList.get(i), null, null));
        }

        TreeNode tN1, tN2, n, currentRoot = null;
        double prob = 0;

        while (!(source.isEmpty() == true && target.size() == 1)) {
            // target queue is empty
            if (target.size() == 0 && source.size()>=2) {
                tN1 = source.dequeue();
                tN2 = source.dequeue();
            }

            // first probability in source <= those in target
            else if (source.size() >0 && source.peek().getData().getProbOcc() <= target.peek().getData().getProbOcc()) {
                tN1 = source.dequeue();
                // second probability in source <= those in target
                if (source.size()>0 && target.size()>0 && source.peek().getData().getProbOcc() <= target.peek().getData().getProbOcc()) {
                    tN2 = source.dequeue();
                } else {
                    tN2 = target.dequeue();
                }
            }

            // first probability in target < those in source
            else {
                tN1 = target.dequeue();
                // second probability in target < those in source
                if (source.size()>0 && target.size()>0 && target.peek().getData().getProbOcc() < source.peek().getData().getProbOcc()) {
                    tN2 = target.dequeue();
                    // OR second probability in source <= those in target
                } else if (source.size()==0 && target.size() >0) {
                    tN2 = target.dequeue();
                }
                else {
                    tN2  = source.dequeue();
                }
            }

            prob = tN1.getData().getProbOcc() + tN2.getData().getProbOcc();
            n = new TreeNode(new CharFreq(null, prob), tN1, tN2);
            target.enqueue(n);
            currentRoot = n;

            tN1 = null;
            tN2 = null;
            n = null;
        }

        huffmanRoot = currentRoot;
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding.
     * Characters not
     * present in the huffman coding tree should have their spots in the array left
     * null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
        /* Your code goes here */
        if (huffmanRoot == null) {
            return;
        }

        // int character = 0;
        encodings = new String[128];
        String code = "";
        ArrayList<String> codes = new ArrayList<String>();
        ArrayList<String> finalCodes = new ArrayList<String>();
        finalCodes = traceHuffmanTree(huffmanRoot, code, codes);

        for (int i = 0; i < finalCodes.size(); i++) {
            // getting last character and converting it into its correponding int value in ASCII
            char character = finalCodes.get(i).charAt(finalCodes.get(i).length() - 1);
            int charValue = (int) character;

            if (charValue < 128) {
                encodings[charValue] = finalCodes.get(i).substring(0,finalCodes.get(i).length()-1);
            }
        }

    }

    // HELPER METHOD
    private ArrayList<String> traceHuffmanTree(TreeNode tN, String code, ArrayList<String> codes) {
        // Print the current node
        if (tN.getData().getCharacter() != null) {
            char c = (char)(tN.getData().getCharacter());
            code += c;
            codes.add(code);
        } else {
            traceHuffmanTree(tN.getLeft(), code + "0", codes);
            traceHuffmanTree(tN.getRight(), code + "1", codes);
        }

        return codes;
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString
     * method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        /* Your code goes here */
        char c; 
        int cToNum; 
        String s = ""; 

        while (StdIn.hasNextChar()){
            c = StdIn.readChar(); 
            cToNum = (int)c; 
            s+=encodings[cToNum];
        }

        writeBitString(encodedFile, s);
    }

    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename  The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding - 1; i++)
            pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1')
                currentByte += 1 << (7 - byteIndex);
            byteIndex++;

            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }

        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        } catch (Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString
     * method
     * to convert the file into a bit string, then decodes the bit string using the
     * tree, and writes it to a decoded file.
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        /* Your code goes here */
        StdOut.setFile(decodedFile);
        String s = readBitString(encodedFile);
        TreeNode tN = huffmanRoot; 
        String word = ""; 

        for (int i = 0; i<s.length(); i++){
            if (s.substring(i,i+1).equals("0") && tN.getLeft()!= null){
                tN = tN.getLeft(); 
                if(tN.getData().getCharacter()!= null){
                    word+=(char)tN.getData().getCharacter();
                    tN = huffmanRoot;
                }
            }
            else if (s.substring(i,i+1).equals("1") && tN.getRight()!= null){
                tN = tN.getRight(); 
                if (tN.getData().getCharacter()!= null){
                    word+=(char)tN.getData().getCharacter();
                    tN = huffmanRoot;
                }
            }
            else if (tN.getLeft()== null && tN.getRight()== null){
                tN = huffmanRoot; 
            }
        }

        StdOut.print(word);
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";

        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();

            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString +
                        String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1')
                    return bitString.substring(i + 1);
            }

            return bitString.substring(8);
        } catch (Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver.
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() {
        return fileName;
    }

    public ArrayList<CharFreq> getSortedCharFreqList() {
        return sortedCharFreqList;
    }

    public TreeNode getHuffmanRoot() {
        return huffmanRoot;
    }

    public String[] getEncodings() {
        return encodings;
    }
}
