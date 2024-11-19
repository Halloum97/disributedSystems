package server;

import java.io.Serializable;

/**
 * LeaderboardEntry: Represents a single entry in the leaderboard.
 * Stores a player's name, total points, and number of logins.
 */
public class LeaderboardEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int points;
    private int logins;

    // Constructor
    public LeaderboardEntry(String name, int points, int logins) {
        this.name = name;
        this.points = points;
        this.logins = logins;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public int getLogins() {
        return logins;
    }

    // Setters
    public void setPoints(int points) {
        this.points = points;
    }

    public void setLogins(int logins) {
        this.logins = logins;
    }

    // Increment logins
    public void incrementLogins() {
        this.logins++;
    }

    // Add points
    public void addPoints(int points) {
        this.points += points;
    }

    @Override
    public String toString() {
        return "LeaderboardEntry{" +
                "name='" + name + '\'' +
                ", points=" + points +
                ", logins=" + logins +
                '}';
    }
}
