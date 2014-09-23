dexparser
=========

- Author: Hui Chen <hui AT hch.im>

About
---

dexparser is a tool for parsing the dex(dalvik executable file) file format.

**Note:**

- This project is created for people to learn the format of the dex file and the
dalvik Java virtual machine. The author does not take any responsibility for any 
illegal use of the code.

TODO
---

1. Parse the debug info of code_item in CodeItem.java.
2. Parse instructions in Instruction.java.
3. Add the toString method to model objects, so that we can print the classes in
the more readable format.
4. Add the command line user interface for parsing and manipulate the dex file.
5. Add the encode and generateChecksum method to encode the DexFile model into 
the dex file with valid checksum.