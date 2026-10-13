package com.stayfinder.model;

/** Starea unei rezervări. Nu ștergem rezervările anulate: le păstrăm pentru istoric. */
public enum BookingStatus {
    CONFIRMED,
    CANCELLED
}
