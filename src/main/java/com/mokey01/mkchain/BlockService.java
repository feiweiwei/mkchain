package com.mokey01.mkchain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: feiweiwei
 * @description: 区块链核心服务类
 * @created Date: 18/4/9.
 * @modify by:
 */
public class BlockService {
    /**
     * 区块链表
    */
    private List<Block> blockChain;

    public BlockService() {
        this.blockChain = new ArrayList<Block>();
        blockChain.add(this.getFristBlock());
    }

    /**
     * 根据index、前置区块hash、区块时间戳、区块数据区计算sha256
     *
     * @author: feiweiwei
     * @params:
     *   * @param index
     * @param previousHash
     * @param timestamp
     * @param data
     * @return: java.lang.String

    */
    private String calculateHash(int index, String previousHash, long timestamp, String data) {
        StringBuilder builder = new StringBuilder(index);
        builder.append(previousHash).append(timestamp).append(data);
        return SHA256Util.getSHA256(builder.toString());
    }

    public
    /**
     * 获取最新的区块
     *
     * @author: feiweiwei
     * @params:
     *   * @param
     * @return: Block

    */
    Block getLatestBlock() {
        return blockChain.get(blockChain.size() - 1);
    }

    private
    /**
     *
     * 创建第一个创世区块
     * @author: feiweiwei
     * @params:
     *   * @param
     * @return: Block

    */
    Block getFristBlock() {
        return new Block(1, "0", System.currentTimeMillis(), "Hello MkBlock!", "fw850919ww10ea0a2cb885078fa9bc2354e55efc81be8f56b66e4a8198411d");
    }

    public
    /**
     * 创建下一个新区块
     *
     * @author: feiweiwei
     * @params:
     *   * @param blockData
     * @return: Block

    */
    Block generateNextBlock(String blockData) {
        Block previousBlock = this.getLatestBlock();
        int nextIndex = previousBlock.getIndex() + 1;
        long nextTimestamp = System.currentTimeMillis();
        String nextHash = calculateHash(nextIndex, previousBlock.getHash(), nextTimestamp, blockData);
        return new Block(nextIndex, previousBlock.getHash(), nextTimestamp, blockData, nextHash);
    }

    public
    /**
     * 将区块上链
     *
     * @author: feiweiwei
     * @params:
     *   * @param newBlock
     * @return: void

    */
    void addBlock(Block newBlock) {
        if (isValidNewBlock(newBlock, getLatestBlock())) {
            //这里简化上链过程，只是将加到区块链表的List中
            blockChain.add(newBlock);
        }
    }

    private
    /**
     * 验证区块是否为有效的新区块
     *
     * @author: feiweiwei
     * @params:
     *   * @param newBlock
     * @param previousBlock
     * @return: boolean

    */
    boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        //验证上一个区块的index＋1是否等于新区块iindex，这里的index算法只是简单＋1，可以用其他复杂算法
        if (previousBlock.getIndex() + 1 != newBlock.getIndex()) {
            System.out.println("invalid index");
            return false;
        } else if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
            //判断前置节点的hash和新节点header中的前置hash值是否一致
            System.out.println("invalid previoushash");
            return false;
        } else {
            //计算新区块的hash值有没有被修改过，在实际过程中，一般通过PKI签名加强保护，并不是简单的sha256
            String hash = calculateHash(newBlock.getIndex(), newBlock.getPreviousHash(), newBlock.getTimestamp(),
                    newBlock.getData());
            if (!hash.equals(newBlock.getHash())) {
                System.out.println("invalid hash: " + hash + " " + newBlock.getHash());
                return false;
            }
        }
        return true;
    }

    public
    /**
     * 以最长的有效区块链为主链，替换之前节点保存的链
     *
     * @author: feiweiwei
     * @params:
     *   * @param newBlocks
     * @return: void

    */
    void replaceChain(List<Block> newBlocks) {
        if (isValidBlocks(newBlocks) && newBlocks.size() > blockChain.size()) {
            blockChain = newBlocks;
        } else {
            System.out.println("Received blockchain invalid");
        }
    }

    private
    /**
     * 验证链上每个节点是否有效
     *
     * @author: feiweiwei
     * @params:
     *   * @param newBlocks
     * @return: boolean

    */
    boolean isValidBlocks(List<Block> newBlocks) {
        Block fristBlock = newBlocks.get(0);
        if (fristBlock.equals(getFristBlock())) {
            return false;
        }

        for (int i = 1; i < newBlocks.size(); i++) {
            if (isValidNewBlock(newBlocks.get(i), fristBlock)) {
                fristBlock = newBlocks.get(i);
            } else {
                return false;
            }
        }
        return true;
    }

    public List<Block> getBlockChain() {
        return blockChain;
    }
}
