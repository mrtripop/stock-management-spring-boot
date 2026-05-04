package com.mrtripop.clinical.models.db;

public enum RegulatorySchedule {
  OTC,
  RX,
  CONTROLLED,
  UNSCHEDULED;

  public boolean isControlled() {
    return this == CONTROLLED;
  }
}
