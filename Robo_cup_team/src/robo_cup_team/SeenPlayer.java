/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package robo_cup_team;

import java.lang.reflect.Field;
import org.apache.log4j.Logger;

/**
 *
 * @author james.grant
 */

//Storage of the seen player inside memory, just returns all of the values
public class SeenPlayer {
    
    private static Logger log           = Logger.getLogger(SeenPlayer.class);
    public int number;
    public boolean goalie;
    public double distance;
    public double direction;
    public double distChange;
    public double dirChange;
    public double bodyFacingDirection;
    public double headFacingDirection;
    public boolean hasBall;
    public boolean isTeammate;
    public double distanceBall;
    public double directionBall;
    public double realDistanceBall;
    
    public SeenPlayer (int num, boolean goal, double dist, double dir, double distC,
                                 double dirC, double bod, double head, boolean teamMate,
                                 double distBall, double dirBall) {
        number = num;
        goalie = goal;
        distance = dist;
        direction = dir;
        distChange = distC;
        dirChange = dirC;
        bodyFacingDirection = bod;
        headFacingDirection = head;
        isTeammate = teamMate;
        hasBall = false;
        distanceBall = distBall;
        directionBall = dirBall;
    }
    
    public void hasBall(boolean set) {
        hasBall = set;
    }
    
    private void calculateBallDistance() {
        //Calculates distance from ball based on cosine
        double angle;
        angle = directionBall - direction;
        
        double cos = Math.pow(distanceBall, 2) + Math.pow(distance, 2) - 2 * (distanceBall*distance) * Math.cos(angle);
                
        directionBall = Math.sqrt(cos);
        
    }
    
}
