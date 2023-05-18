package com.example.mada.huffmann;

import java.util.Comparator;

public class TreeNode implements Comparable<TreeNode> {
    private String asciiSymbol;
    private double occurrence;
    private TreeNode left;
    private TreeNode right;

    public static TreeNode mergeNode(TreeNode a, TreeNode b) {
        return new TreeNode(
                a.getOccurrence() + b.getOccurrence(),
                a,
                b
        );
    }

    public TreeNode(double occurrence, TreeNode left, TreeNode right) {
        this.occurrence = occurrence;
        this.left = left;
        this.right = right;
    }

    public TreeNode(String asciiSymbol, double occurrence) {
        this.asciiSymbol = asciiSymbol;
        this.occurrence = occurrence;
    }

    @Override
    public int compareTo(TreeNode o) {
        return Comparator.comparingDouble(TreeNode::getOccurrence)
                .compare(this, o);
    }

    public boolean isALeaf() {
        return asciiSymbol != null;
    }

    public String getAsciiSymbol() {
        return asciiSymbol;
    }

    public double getOccurrence() {
        return occurrence;
    }

    public TreeNode getLeft() {
        return left;
    }

    public TreeNode getRight() {
        return right;
    }
}
