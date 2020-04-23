#!/usr/bin/python3
# -*- coding: utf-8 -*-
# ComputeSum 
# generated by Structorizer 3.30-08 

# Copyright (C) 2020-03-21 Kay Gürtzig 
# License: GPLv3-link 
# GNU General Public License (V 3) 
# https://www.gnu.org/licenses/gpl.html 
# http://www.gnu.de/documents/gpl.de.html 

from enum import Enum
import math

# Tries to read as many integer values as possible upto maxNumbers 
# from file fileName into the given array numbers. 
# Returns the number of the actually read numbers. May cause an exception. 
def readNumbers(fileName, numbers, maxNumbers) :
    nNumbers = 0
    fileNo = fileOpen(fileName)
    if (fileNo <= 0):
        raise Exception( "File could not be opened!")

    try:
        try:
            while (not  fileEOF(fileNo)  and  nNumbers < maxNumbers):
                number = fileReadInt(fileNo)
                numbers[nNumbers] = number
                nNumbers = nNumbers + 1

        finally:
            fileClose(fileNo)
    except Exception, error:
        raise Exception()
    
    return nNumbers

# Computes the sum and average of the numbers read from a user-specified 
# text file (which might have been created via generateRandomNumberFile(4)). 
#  
# This program is part of an arrangement used to test group code export (issue 
# #828) with FileAPI dependency. 
# The input check loop has been disabled (replaced by a simple unchecked input 
# instruction) in order to test the effect of indirect FileAPI dependency (only the 
# called subroutine directly requires FileAPI now). 
fileNo = 1000
# Disable this if you enable the loop below! 
file_name = input("Name/path of the number file")
# If you enable this loop, then the preceding input instruction is to be disabled 
# and the fileClose instruction in the alternative below is to be enabled. 
# while True: 
#     file_name = input("Name/path of the number file") 
#     fileNo = fileOpen(file_name) 
#     if fileNo > 0  or  file_name == "": 
#         break 
#  
if (fileNo > 0):
    # This should be enabled if the input check loop above gets enabled. 
#     fileClose(fileNo) 
    values = []
    nValues = 0
    try:
        nValues = readNumbers(file_name, values, 1000)
    except Exception, failure:
        print(failure, sep='')
        # FIXME: unsupported jump/exit instruction! 
        # exit -7 
    
    sum = 0.0
    for k in range(0, nValues-1+1, 1):
        sum = sum + values[k]

    print("sum = ", sum, sep='')
    print("average = ", sum / nValues, sep='')
