<?php
// program SORTING_TEST_MAIN (generated by Structorizer 3.30-13) 

// Copyright (C) 2019-10-02 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

// function bubbleSort 
// Implements the well-known BubbleSort algorithm. 
// Compares neigbouring elements and swaps them in case of an inversion. 
// Repeats this while inversions have been found. After every 
// loop passage at least one element (the largest one out of the 
// processed subrange) finds its final place at the end of the 
// subrange. 
function bubbleSort($values)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	$ende = length($values) - 2;
	do
	{
		// The index of the most recent swapping (-1 means no swapping done). 
		$posSwapped = -1;
		for ($i = 0; $i <= $ende; $i += (1))
		{
			if ($values[$i] > $values[$i+1])
			{
				$temp = $values[$i];
				$values[$i] = $values[$i+1];
				$values[$i+1] = $temp;
				$posSwapped = $i;
			}
		}
		$ende = $posSwapped - 1;
	} while (!( $posSwapped < 0 ));
}

// function maxHeapify 
// Given a max-heap 'heap´ with element at index 'i´ possibly 
// violating the heap property wrt. its subtree upto and including 
// index range-1, restores heap property in the subtree at index i 
// again. 
function maxHeapify($heap, $i, $range)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	// Indices of left and right child of node i 
	$right = ($i+1) * 2;
	$left = $right - 1;
	// Index of the (local) maximum 
	$max = $i;
	if ($left < $range && $heap[$left] > $heap[$i])
	{
		$max = $left;
	}
	if ($right < $range && $heap[$right] > $heap[$max])
	{
		$max = $right;
	}
	if ($max != $i)
	{
		$temp = $heap[$i];
		$heap[$i] = $heap[$max];
		$heap[$max] = $temp;
		maxHeapify($heap, $max, $range);
	}
}

// function partition 
// Partitions array 'values´ between indices 'start´ und 'stop´-1 with 
// respect to the pivot element initially at index 'p´ into smaller 
// and greater elements. 
// Returns the new (and final) index of the pivot element (which 
// separates the sequence of smaller elements from the sequence 
// of greater elements). 
// This is not the most efficient algorithm (about half the swapping 
// might still be avoided) but it is pretty clear. 
function partition($values, $start, $stop, $p)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	// Cache the pivot element 
	$pivot = $values[$p];
	// Exchange the pivot element with the start element 
	$values[$p] = $values[$start];
	$values[$start] = $pivot;
	$p = $start;
	// Beginning and end of the remaining undiscovered range 
	$start = $start + 1;
	$stop = $stop - 1;
	// Still unseen elements? 
	// Loop invariants: 
	// 1. p = start - 1 
	// 2. pivot = values[p] 
	// 3. i < start → values[i] ≤ pivot 
	// 4. stop < i → pivot < values[i] 
	while ($start <= $stop) 
	{
		// Fetch the first element of the undiscovered area 
		$seen = $values[$start];
		// Does the checked element belong to the smaller area? 
		if ($seen <= $pivot)
		{
			// Insert the seen element between smaller area and pivot element 
			$values[$p] = $seen;
			$values[$start] = $pivot;
			// Shift the border between lower and undicovered area, 
			// update pivot position. 
			$p = $p + 1;
			$start = $start + 1;
		}
		else
		{
			// Insert the checked element between undiscovered and larger area 
			$values[$start] = $values[$stop];
			$values[$stop] = $seen;
			// Shift the border between undiscovered and larger area 
			$stop = $stop - 1;
		}
	}
	return $p;
}

// function testSorted 
// Checks whether or not the passed-in array is (ascendingly) sorted. 
function testSorted($numbers)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	$isSorted = true;
	$i = 0;
	// As we compare with the following element, we must stop at the penultimate index 
	while ($isSorted && ($i <= length($numbers)-2)) 
	{
		// Is there an inversion? 
		if ($numbers[$i] > $numbers[$i+1])
		{
			$isSorted = false;
		}
		else
		{
			$i = $i + 1;
		}
	}
	return $isSorted;
}

// function buildMaxHeap 
// Runs through the array heap and converts it to a max-heap 
// in a bottom-up manner, i.e. starts above the "leaf" level 
// (index >= length(heap) div 2) and goes then up towards 
// the root. 
function buildMaxHeap($heap)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	$lgth = length($heap);
	for ($k = $lgth / 2 - 1; $k >= 0; $k += (-1))
	{
		maxHeapify($heap, $k, $lgth);
	}

	return 0;
}

// function quickSort 
// Recursively sorts a subrange of the given array 'values´.  
// start is the first index of the subsequence to be sorted, 
// stop is the index BEHIND the subsequence to be sorted. 
function quickSort($values, $start, $stop)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	// At least 2 elements? (Less don't make sense.) 
	if ($stop >= $start + 2)
	{
		// Select a pivot element, be p its index. 
		// (here: randomly chosen element out of start ... stop-1) 
		$p = rand(0, ($stop-$start) - 1) + $start;
		// Partition the array into smaller and greater elements 
		// Get the resulting (and final) position of the pivot element 
		$p = partition($values, $start, $stop, $p);
			// Sort left (lower) array part 
			quickSort($values, $start, $p);
			// Sort right (higher) array part 
			quickSort($values, $p+1, $stop);
	}

	return 0;
}

// function heapSort 
// Sorts the array 'values´ of numbers according to he heap sort 
// algorithm 
function heapSort($values)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	buildMaxHeap($values);
	$heapRange = length($values);
	for ($k = $heapRange - 1; $k >= 1; $k += (-1))
	{
		$heapRange = $heapRange - 1;
		// Swap the maximum value (root of the heap) to the heap end 
		$maximum = $values[0];
		$values[0] = $values[$heapRange];
		$values[$heapRange] = $maximum;
		maxHeapify($values, 0, $heapRange);
	}

	return 0;
}
// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

// program SORTING_TEST_MAIN 
// Creates three equal arrays of numbers and has them sorted with different sorting algorithms 
// to allow performance comparison via execution counting ("Collect Runtime Data" should 
// sensibly be switched on). 
// Requested input data are: Number of elements (size) and filing mode. 

// TODO Establish sensible web formulars to get the $_GET input working. 

do
{
	$elementCount = $_REQUEST['elementCount'];	// TODO form a sensible input opportunity;
} while (!( $elementCount >= 1 ));
do
{
	$modus = $_REQUEST["Filling: 1 = random, 2 = increasing, 3 = decreasing"];	// TODO form a sensible input opportunity;
} while (!( $modus == 1 || $modus == 2 || $modus == 3 ));
for ($i = 0; $i <= $elementCount-1; $i += (1))
{
	switch ($modus) 
	{
		case 1:
			$values1[$i] = rand(0, (10000) - 1);
			break;
		case 2:
			$values1[$i] = $i;
			break;
		case 3:
			$values1[$i] = -$i;
			break;
	}
}
// Copy the array for exact comparability 
for ($i = 0; $i <= $elementCount-1; $i += (1))
{
	$values2[$i] = $values1[$i];
	$values3[$i] = $values1[$i];
}
	bubbleSort($values1);
	quickSort($values2, 0, $elementCount);
	heapSort($values3);
$ok1 = testSorted($values1);
$ok2 = testSorted($values2);
$ok3 = testSorted($values3);
if (! $ok1 || ! $ok2 || ! $ok3)
{
	for ($i = 0; $i <= $elementCount-1; $i += (1))
	{
		if ($values1[$i] != $values2[$i] || $values1[$i] != $values3[$i])
		{
			echo "Difference at [", $i, "]: ", $values1[$i], " <-> ", $values2[$i], " <-> ", $values3[$i];
		}
	}
}
do
{
	$show = $_REQUEST["Show arrays (yes/no)?"];	// TODO form a sensible input opportunity;
} while (!( $show == "yes" || $show == "no" ));
if ($show == "yes")
{
	for ($i = 0; $i <= $elementCount - 1; $i += (1))
	{
		echo "[", $i, "]:\t", $values1[$i], "\t", $values2[$i], "\t", $values3[$i];
	}
}

?>
