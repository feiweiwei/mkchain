# 动手写个区块链－MkChain

## 前言

之前写过两篇关于区块链的文章，今天写的是第三篇是写一个简单的区块链，通过实现最核心的模块来让整个区块链转起来，其中涉及到节点分布式的管理、区块的相关服务，代码量不大也就几百行代码，看起来也比较容易，一些区块链里的基本概念可以先参考之前写的文章，这个版本是java写的后续也会有python的版本，下面直接上干货，最后会有git源码放出。

## 定义区块

```java
/**
 * @author: feiweiwei
 * @description: 区块bean
 * @created Date: 18/4/9.
 * @modify by:
 */
public class Block {
    private int    index;
    private String previousHash;
    private long   timestamp;
    private String data;
    private String hash;

  //省略构造函数和getter、setter函数
 ...
｝
```



## 实现区块链核心服务

```java
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
```



## 定义P2P分布式消息

```java
public class Message implements Serializable{
    private int    type;
    private String data;
//省略构造函数和getter、setter函数
}
```



## 实现分布式基础服务

```java
/**
 * @author: feiweiwei
 * @description: 区块链节点P2P服务类
 * @created Date: 18/4/9.
 * @modify by:
 */
public class P2PService {
    private List<WebSocket> sockets;
    private BlockService    blockService;
    private final static int QUERY_LATEST        = 0;
    private final static int QUERY_ALL           = 1;
    private final static int RESPONSE_BLOCKCHAIN = 2;

    public P2PService(BlockService blockService) {
        this.blockService = blockService;
        this.sockets = new ArrayList<WebSocket>();
    }

    public void initP2PServer(int port) {
        //定义websocketserver，重写相关回调
        final WebSocketServer socket = new WebSocketServer(new InetSocketAddress(port)) {
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                write(webSocket, queryChainLengthMsg());
                //将启动的节点加入区块链 websocket list中
                sockets.add(webSocket);
            }

            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                //websocket长链接断后触发onClose回调，将webSocket实例从list中移除
                System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
                sockets.remove(webSocket);
            }

            public void onMessage(WebSocket webSocket, String s) {
                handleMessage(webSocket, s);
            }

            public void onError(WebSocket webSocket, Exception e) {
                //websocket长链接出现异常后触发onError回调，将webSocket实例从list中移除
                System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
                sockets.remove(webSocket);
            }

            public void onStart() {

            }
        };
        socket.start();
        System.out.println("listening websocket p2p port on: " + port);
    }

    private
    /**
     * 根据收到的消息类型进行相应处理
     *
     * @author: feiweiwei
     * @params:
     *   * @param webSocket
     * @param s
     * @return: void

    */
    void handleMessage(WebSocket webSocket, String s) {
        try {
            Message message = JSON.parseObject(s, Message.class);
            System.out.println("Received message" + JSON.toJSONString(message));
            switch (message.getType()) {
                case QUERY_LATEST:
                    write(webSocket, responseLatestMsg());
                    break;
                case QUERY_ALL:
                    write(webSocket, responseChainMsg());
                    break;
                case RESPONSE_BLOCKCHAIN:
                    handleBlockChainResponse(message.getData());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.out.println("hanle message is error:" + e.getMessage());
        }
    }

    private void handleBlockChainResponse(String message) {
        List<Block> receiveBlocks = JSON.parseArray(message, Block.class);
        //将从消息中获取到的区块链中所有的区块节点，按照index顺序进行排序，进行排序是为了防止节点传递的区块链是无序的
        Collections.sort(receiveBlocks, new Comparator<Block>() {
            public int compare(Block o1, Block o2) {
                return o1.getIndex() - o1.getIndex();
            }
        });

        Block latestBlockReceived = receiveBlocks.get(receiveBlocks.size() - 1);
        Block latestBlock = blockService.getLatestBlock();

        //将从其他节点获取到的区块链最新节点和本节点最新的节点进行比较
        if (latestBlockReceived.getIndex() > latestBlock.getIndex()) {
            //本地最新节点的hash等于传递主链中的最新区块的上一个区块的hash则将数据验证后上链
            if (latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
                System.out.println("We can append the received block to our chain");
                blockService.addBlock(latestBlockReceived);
                //新区块上链后广播通知所有节点最新区块
                broatcast(responseLatestMsg());
            } else if (receiveBlocks.size() == 1) {
                //节点为新节点则去要广播所有节点去查询区块
                System.out.println("We have to query the chain from our peer");
                broatcast(queryAllMsg());
            } else {
                //区块hash不匹配则进行区块链替换
                blockService.replaceChain(receiveBlocks);
            }
        } else {
            System.out.println("received blockchain is not longer than received blockchain. Do nothing");
        }
    }

    public
    /**
     * websocketclient连接到server节点
     *
     * @author: feiweiwei
     * @params:
     *   * @param peer
     * @return: void

    */
    void connectToPeer(String peer) {
        try {
            final WebSocketClient socket = new WebSocketClient(new URI(peer)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    write(this, queryChainLengthMsg());
                    sockets.add(this);
                }

                @Override
                public void onMessage(String s) {
                    handleMessage(this, s);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    System.out.println("connection failed");
                    sockets.remove(this);
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("connection failed");
                    sockets.remove(this);
                }
            };
            socket.connect();
        } catch (URISyntaxException e) {
            System.out.println("p2p connect is error:" + e.getMessage());
        }
    }

    private void write(WebSocket ws, String message) {
        ws.send(message);
    }

    public
    /**
     * 向所有上链节点广播消息
     *
     * @author: feiweiwei
     * @params:
     *   * @param message
     * @return: void

    */
    void broatcast(String message) {
        for (WebSocket socket : sockets) {
            this.write(socket, message);
        }
    }

    private String queryAllMsg() {
        return JSON.toJSONString(new Message(QUERY_ALL));
    }

    private String queryChainLengthMsg() {
        return JSON.toJSONString(new Message(QUERY_LATEST));
    }

    private String responseChainMsg() {
        return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blockService.getBlockChain())));
    }

    public String responseLatestMsg() {
        Block[] blocks = {blockService.getLatestBlock()};
        return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blocks)));
    }

    public List<WebSocket> getSockets() {
        return sockets;
    }
}


```



## 实现restful API

这里为了简单，使用jetty作为应用容器，定义了增加节点、查询所有节点、挖矿、获取主链这几个servlet作为restful api接口，当然restful服务也可以使用springboot来实现，核心代码一样，只是要写几个controller，这里就不写了。

```java
public class HTTPService {
    private BlockService blockService;
    private P2PService   p2pService;

    public HTTPService(BlockService blockService, P2PService p2pService) {
        this.blockService = blockService;
        this.p2pService = p2pService;
    }

    public void initHTTPServer(int port) {
        try {
            Server server = new Server(port);
            System.out.println("listening http port on: " + port);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            context.addServlet(new ServletHolder(new BlocksServlet()), "/blocks");
            context.addServlet(new ServletHolder(new MineBlockServlet()), "/mineBlock");
            context.addServlet(new ServletHolder(new PeersServlet()), "/peers");
            context.addServlet(new ServletHolder(new AddPeerServlet()), "/addPeer");
            server.start();
            server.join();
        } catch (Exception e) {
            System.out.println("init http server is error:" + e.getMessage());
        }
    }

    private class BlocksServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().println(JSON.toJSONString(blockService.getBlockChain()));
        }
    }


    private class AddPeerServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            this.doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            String peer = req.getParameter("peer");
            p2pService.connectToPeer(peer);
            resp.getWriter().print("ok");
        }
    }


    private class PeersServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            for (WebSocket socket : p2pService.getSockets()) {
                InetSocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
                resp.getWriter().print(remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort());
            }
        }
    }


    private class MineBlockServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            this.doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            String data = req.getParameter("data");
            Block newBlock = blockService.generateNextBlock(data);
            blockService.addBlock(newBlock);
            p2pService.broatcast(p2pService.responseLatestMsg());
            String s = JSON.toJSONString(newBlock);
            System.out.println("block added: " + s);
            resp.getWriter().print(s);
        }
    }
}
```



## 运行

下面我们来运行一下看看效果。

1. 获取代码 git clone

2. 通过命令行到mkchain工程目录，执行 `mvn clean package`如果机器没装maven自行解决

3. 启动核心节点 `java -jar mkchain.jar 8080 7071`

   ```shell
   target git:(master) ✗ java -jar mkchain.jar 8080 7001
   listening websocket p2p port on: 7001
   listening http port on: 8080
   2018-04-09 14:15:48.943:INFO:oejs.Server:main: jetty-9.0.0.v20130308
   2018-04-09 14:15:48.972:INFO:oejsh.ContextHandler:main: started o.e.j.s.ServletContextHandler@6d1e7682{/,null,AVAILABLE}
   2018-04-09 14:15:48.975:INFO:oejs.ServerConnector:main: Started ServerConnector@2a3046da{HTTP/1.1}{0.0.0.0:8080}
   ```

4. 启动其他上链节点`java -jar mkchain.jar 8081 7072 ws://localhost:7071`

   ```shell
   target git:(master) ✗ java -jar mkchain.jar 8081 7002 ws://localhost:7001
   listening websocket p2p port on: 7002
   listening http port on: 8081
   2018-04-09 14:17:01.212:INFO:oejs.Server:main: jetty-9.0.0.v20130308
   2018-04-09 14:17:01.443:INFO:oejsh.ContextHandler:main: started o.e.j.s.ServletContextHandler@5479e3f{/,null,AVAILABLE}
   2018-04-09 14:17:01.455:INFO:oejs.ServerConnector:main: Started ServerConnector@4362abb7{HTTP/1.1}{0.0.0.0:8081}
   Received message{"type":0}
   Received message{"data":"[{\"data\":\"Hello MkBlock!\",\"hash\":\"fw850919ww10ea0a2cb885078fa9bc2354e55efc81be8f56b66e4a8198411d\",\"index\":1,\"previousHash\":\"0\",\"timestamp\":1523254548747}]","type":2}
   received blockchain is not longer than received blockchain. Do nothing
   ```

   ​

5. 通过curl调用restful api进行挖矿`curl -H "Content-type:application/json" --data '{"data" : "Some data to the first block"}' http://localhost:8080/mineBlock`, 当然不用curl也可以用类似postman这些发包工具也是一样的

   ```shell
   ➜  ~ curl -H "Content-type:application/json" --data '{"data" : "Some data to the first block"}' http://localhost:8080/mineBlock
   {"hash":"d8413bb10003edf0e666d2b6801018d2b01330b8d83d9b1d1e09be8bafa430fb","index":2,"previousHash":"fw850919ww10ea0a2cb885078fa9bc2354e55efc81be8f56b66e4a8198411d","timestamp":1523252857170}% 
   ```

   可以看到增加了一个区块到主链上，其他两个窗口也会有相应的提示。

## 总结

上面核心代码都已经写好了，通过上面这些代码其实一个超级简化版的区块链雏形已经出来了，这个简化版区块链对于理解区块链的核心思想和架构是很有帮助的，能够让刚开始接触区块链的程序员通过代码能够在最短的时间了解区块链的核心思想。目前开源的一些区块链，其实底层也是由这些模块组成，只不过比较成熟的区块链系统中包含了完备的异常处理和非常好的健壮性，而且在业务可扩展性方面也更完善。