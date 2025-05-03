package server;

import lombok.extern.slf4j.Slf4j;
import model.email.EmailManager;
import model.email.EmailManagerImpl;
import model.user.UserManager;
import model.user.UserManagerImpl;
import service.EmailUtils;
import service.ServiceClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EmailServer {

    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 10;
    private static final long KEEP_ALIVE_TIME = 30L;
    private static final int QUEUE_CAPACITY = 50;


    public static void main(String[] args) {



        ExecutorService clientHandlerPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY)
        );

        try (ServerSocket connectionSocket = new ServerSocket(EmailUtils.PORT)

        ) {
            log.info("Server started on port " + EmailUtils.PORT);


            EmailManager emailManager = new EmailManagerImpl();
            UserManager userManager = new UserManagerImpl();

            boolean validServerSession = true;
            while(validServerSession){
                Socket clientDataSocket = connectionSocket.accept();
                ServiceClientHandler clientHandler = new ServiceClientHandler(clientDataSocket, emailManager, userManager);
                clientHandlerPool.submit(clientHandler);
            }

        } catch (IOException e) {
            log.error("Connection socket cannot be established:" + e.getMessage());
        } finally {
            clientHandlerPool.shutdown();
        }
    }

}
