package learning.multithreading;

import org.springframework.stereotype.Service;

@Service
public class RunnableService implements Runnable {
  @Override
  public void run() {
    System.out.println("Runnable is running");
  }
}
