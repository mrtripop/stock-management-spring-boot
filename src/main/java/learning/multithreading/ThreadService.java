package learning.multithreading;

import org.springframework.stereotype.Service;

@Service
public class ThreadService extends Thread {
  @Override
  public void run() {
    System.out.println("Thread is running");
  }
}
