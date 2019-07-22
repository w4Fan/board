package board.core.entity;

import lombok.Data;

@Data
public class Project {

  private int id;

  public Project() {
  }

  public Project(int id) {
    this.id = id;
  }
}
