package net.nanopool.contention;

public class ActiveResizingContentionHandler {

  static class Actor extends Thread {

    public void shutdown() {
      // TODO Auto-generated method stub
      
    }
    
  }

  private final Actor actor;

  ActiveResizingContentionHandler(Actor actor) {
    this.actor = actor;
  }

  public void start() {
    actor.start();
  }

  public void stop() {
    actor.shutdown();
  }
  
}
