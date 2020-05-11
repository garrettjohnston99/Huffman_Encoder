# Huffman Encoder

Huffman encoding is a lossless compression technique that assigns unique binary codes to characters in a file based on how frequently they are used. Characters that appear more frequently have shorter codes, making compression and decompression efficient as opposed to simpler methods.

#### HuffEncoder.java
Contains methods to select a file, find character frequencies, create a priority queue of HuffElement objects, construct a Huffman Code tree, create a Huffman Code map, and finally compress/decompress the file. 

#### BinaryTree.java
Generic binary tree class. In this implementation, stored data are HuffElement objects.

#### BufferedBitReader.java
Bit reader used to decompress files. Assumes that the last byte of the file contains the number of valid bits in the previous byte.

#### BufferedBitWriter.java
Bit writer used to compress files. Accumulates bits until gets a byte, then writes it. On closing writes an additional byte holding the number of valid bits in the final byte written.

#### HuffElement.java
Class to store character and frequency pairs.



###### Acknowledgements
BinaryTree.java, BufferedBitReader.java, BufferedBitWriter.java provided in CS10: Problem Solving via Object-Oriented Programming.
