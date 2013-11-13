package robo_cup_team;


//~--- non-JDK imports --------------------------------------------------------

import com.github.robocup_atan.atan.model.ActionsPlayer;
import com.github.robocup_atan.atan.model.ControllerPlayer;
import com.github.robocup_atan.atan.model.enums.Errors;
import com.github.robocup_atan.atan.model.enums.Flag;
import com.github.robocup_atan.atan.model.enums.Line;
import com.github.robocup_atan.atan.model.enums.Ok;
import com.github.robocup_atan.atan.model.enums.PlayMode;
import com.github.robocup_atan.atan.model.enums.RefereeMessage;
import com.github.robocup_atan.atan.model.enums.ServerParams;
import com.github.robocup_atan.atan.model.enums.ViewAngle;
import com.github.robocup_atan.atan.model.enums.ViewQuality;
import com.github.robocup_atan.atan.model.enums.Warning;
import java.util.ArrayList;

import org.apache.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * A simple controller. It implements the following simple behaviour. If the
 * client sees nothing (it might be out of the field) it turns 180 degree. If
 * the client sees the own goal and the distance is less than 40 and greater
 * than 10 it turns to his own goal and dashes. If it cannot see the own goal
 * but can see the ball it turns to the ball and dashes. If it sees anything but
 * not the ball or the own goals it dashes a little bit and turns a fixed amount
 * of degree to the right.
 *
 * @author Atan
 */
public class Simple implements ControllerPlayer {
    private static int    count         = 0;
    private static Logger log           = Logger.getLogger(Simple.class);
    private Random        random        = null;
    private boolean       canSeeOwnGoal = false;
    private boolean       canSeeNothing = true;
    private boolean       canSeeBall    = false;
    private double        directionBall;
    private double        directionOwnGoal;
    private double        distanceBall;
    private double        distanceOwnGoal;
    private ActionsPlayer player;
    
    //JG variables
    private List<SeenPlayer> jgAllPlayers = new ArrayList<>();
    
    static final int      jgWALKSPEED = 20;
    static final int      jgJOGSPEED = 50;
    static final int      jgRUNSPEED = 70;
    static final int      jgSPRINTSPEED = 100;
    static double         jgPOSSESSIONDISTANCE = 1.5;
    
    private double        jgFinalKickDirection = 0.0;
    private int           jgFinalKickPower = 0;
    private int           jgFinalRunPower = 100;
    private double        jgFinalTurnDirection = 0;
    private boolean       jgPlayOn = false;
    private boolean       jgMakeRunForBall = true;
    private boolean       jgCanSeeOpponentGoal = false;
    private boolean       jgInPossession = false;
    private boolean       jgUpdatedThePlayerList = false;
    private double        jgDirectionOpponentGoal;
    private SeenPlayer    jgPlayerWithTheBall;
    
    /**
     * Constructs a new simple client.
     */
    public Simple() {
        random = new Random(System.currentTimeMillis() + count);
        count++;
    }

    /** {@inheritDoc} */
    @Override
    public ActionsPlayer getPlayer() {
        return player;
    }

    /** {@inheritDoc} */
    @Override
    public void setPlayer(ActionsPlayer p) {
        player = p;
    }

    /** {@inheritDoc} */
    @Override
    public void preInfo() {
        canSeeOwnGoal = false;
        canSeeBall    = false;
        canSeeNothing = true;
        jgInPossession = false;
    
        jgFinalKickDirection = 0.0;
        jgFinalKickPower = 0;
        jgFinalRunPower = 60;
        jgFinalTurnDirection = 0;
        jgMakeRunForBall = true;
        jgCanSeeOpponentGoal = false;
        jgUpdatedThePlayerList = false;
    }

    /** {@inheritDoc} */
    @Override
    public void postInfo() {
        switch (this.getPlayer().getNumber()) {
                case 2 : case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:
                jgFindClosestPlayerToBall();
                if (canSeeNothing) {
                    log.info("Can see nothing");
                    canSeeNothingAction();
                } else if (canSeeOwnGoal) {
                    if ((distanceOwnGoal < 40)) {
                        log.info("Can see own goal");
                        canSeeOwnGoalAction();
                    } else if (canSeeBall) {
                        log.info("Can see ball");
                        canSeeBallAction();
                    } else {
                        log.info("Can see Anything");
                        canSeeAnythingAction();
                    }
                }  else if (canSeeBall) {
                    log.info("Can see Ball");
                    canSeeBallAction();
                } else {
                    log.info("Can see Anything");
                    canSeeAnythingAction();
                }
                if (jgFinalTurnDirection != 0) {
                    this.getPlayer().turn(jgFinalTurnDirection);
                }
                if (jgFinalRunPower != 0.0) {
                    if (this.jgPlayOn == true) {
                        this.getPlayer().dash(jgFinalRunPower);                        
                    } else if (this.jgPlayOn == false) {
                        if (this.distanceBall > jgPOSSESSIONDISTANCE) {
                            this.getPlayer().dash(jgFinalRunPower);                            
                        }                        
                    }
                }
                if (jgFinalKickPower != 0 && jgPlayOn && jgInPossession && distanceBall < jgPOSSESSIONDISTANCE) {
                    this.getPlayer().kick(jgFinalKickPower, jgFinalKickDirection);
                }
                break;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagRight(Flag flag, double distance, double direction, double distChange, double dirChange,
                                 double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagLeft(Flag flag, double distance, double direction, double distChange, double dirChange,
                                double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
                               double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagOther(Flag flag, double distance, double direction, double distChange, double dirChange,
                                 double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagCenter(Flag flag, double distance, double direction, double distChange, double dirChange,
                                  double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagCornerOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
                                     double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagCornerOther(Flag flag, double distance, double direction, double distChange,
                                       double dirChange, double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagPenaltyOwn(Flag flag, double distance, double direction, double distChange,
                                      double dirChange, double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagPenaltyOther(Flag flag, double distance, double direction, double distChange,
            double dirChange, double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagGoalOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
                                   double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        if (flag == Flag.CENTER) {
            this.canSeeOwnGoal    = true;
            this.distanceOwnGoal  = distance;
            this.directionOwnGoal = direction;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagGoalOther(Flag flag, double distance, double direction, double distChange, double dirChange,
                                     double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        jgCanSeeOpponentGoal = true;
        jgDirectionOpponentGoal = direction;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeLine(Line line, double distance, double direction, double distChange, double dirChange,
                            double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeBall(double distance, double direction, double distChange, double dirChange,
                            double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing      = false; 
        this.canSeeBall    = true;
        this.distanceBall  = distance;
        this.directionBall = direction;
        switch (this.getPlayer().getNumber()) {
                case 2 : case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:
                log.info(distance + ":distance to ball"); 
                if (distance < jgPOSSESSIONDISTANCE) {
                    log.info("inPossession"); 
                    this.jgInPossession = true;
                } else {
                    this.jgInPossession = false;
                }
                break;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOther(int number, boolean goalie, double distance, double direction, double distChange,
                                   double dirChange, double bodyFacingDirection, double headFacingDirection) {
        switch (this.getPlayer().getNumber()) {
                case 2 : case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:
                SeenPlayer seenPlayer = new SeenPlayer(number, goalie, distance, direction, distChange,
                                         dirChange, bodyFacingDirection, headFacingDirection, false);
                if (canSeeBall == true) {
                    seenPlayer.calculateDistance(distanceBall, distance, directionBall, direction);
                    log.info("No. " + seenPlayer.number + " Distance to ball: " + seenPlayer.distanceFromBall);
                } else {
                    seenPlayer.distanceFromBall = 1000;
                }
                boolean found = false;
                for (int i = 0; i<jgAllPlayers.size(); ++i) {
                    SeenPlayer player = jgAllPlayers.get(i);
                    if (player.number == number && player.isTeammate == false) {
                        jgAllPlayers.set(i, seenPlayer);
                        found = true;
                        jgUpdatedThePlayerList = true;
                    }
                }
                if (found == false) {
                    jgAllPlayers.add(seenPlayer);
                    jgUpdatedThePlayerList = true;
                }
                log.info("Array Size, opp: " + jgAllPlayers.size());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange,
                                 double dirChange, double bodyFacingDirection, double headFacingDirection) {
        switch (this.getPlayer().getNumber()) {
                case 2 : case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:
                SeenPlayer seenPlayer = new SeenPlayer(number, goalie, distance, direction, distChange,
                                         dirChange, bodyFacingDirection, headFacingDirection, true);
                jgPlayerWithTheBall = seenPlayer;
                if (canSeeBall == true) {
                    seenPlayer.calculateDistance(distanceBall, distance, directionBall, direction);
                    log.info("No. " + seenPlayer.number + " Distance to ball: " + seenPlayer.distanceFromBall);
                } else {
                    seenPlayer.distanceFromBall = 1000;
                }
                boolean found = false;
                for (int i = 0; i < jgAllPlayers.size(); ++i) {
                    SeenPlayer player = jgAllPlayers.get(i);
                    if (player.number == number && player.isTeammate == true) {
                        jgAllPlayers.set(i, seenPlayer);
                        found = true;
                        jgUpdatedThePlayerList = true;
                    }
                    
                }
                if (found == false) {
                    jgAllPlayers.add(seenPlayer);
                    jgUpdatedThePlayerList = true;
                }
                log.info("Array Size, own: " + jgAllPlayers.size());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoHearReferee(RefereeMessage refereeMessage) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearPlayMode(PlayMode playMode) {
        if (playMode == PlayMode.BEFORE_KICK_OFF) {
            this.pause(1000);
            switch (this.getPlayer().getNumber()) {
                case 1 :
                    this.getPlayer().move(-50, 0);                 
                    break;
                case 2 :
                    this.getPlayer().move(-10, 10); 
                    break;
                case 3 :
                    this.getPlayer().move(-10, -10);                     
                    break;
                case 4 :
                    this.getPlayer().move(-20, 0);
                    break;
                case 5 :
                    this.getPlayer().move(-25, 10);
                    break;
                case 6 :
                    this.getPlayer().move(-25, -10);
                    break;
                case 7 :
                    this.getPlayer().move(-20, 20);
                    break;
                case 8 :
                    this.getPlayer().move(-20, -20);
                    break;
                case 9 :
                    this.getPlayer().move(-36, 0);
                    break;
                case 10 :
                    this.getPlayer().move(-30, 15);
                    break;
                case 11 :
                    this.getPlayer().move(-30, -15);
                    break;
                default :
                    throw new Error("number must be initialized before move");
            }
        } else if (playMode == PlayMode.PLAY_ON) {
            
            switch (this.getPlayer().getNumber()) {
                case 1 :
                    this.getPlayer().move(-50, 0);                 
                    break;
                case 2 : case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:

                    this.jgPlayOn = true;                     
                    break;
            }
        } else if (playMode != PlayMode.PLAY_ON) {
                this.jgPlayOn = false; 
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoHearPlayer(double direction, String message) {
            log.info("Hear Number: " + this.getPlayer().getNumber());
            switch (this.getPlayer().getNumber()) {
                case 1 :
                    this.getPlayer().move(-50, 0);                 
                    break;
                case 2 : case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:
                    log.info("Hear Message: " + message);
                    if (message == "going for ball") {
                        log.info("Hear Message: " + "going for ball");
                        this.jgMakeRunForBall = false;
                    }    
                default :
                    throw new Error("number must be initialized before move");
            }
    }

    /** {@inheritDoc} */
    @Override
    public void infoSenseBody(ViewQuality viewQuality, ViewAngle viewAngle, double stamina, double unknown,
                              double effort, double speedAmount, double speedDirection, double headAngle,
                              int kickCount, int dashCount, int turnCount, int sayCount, int turnNeckCount,
                              int catchCount, int moveCount, int changeViewCount) {}

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return "Simple";
    }

    /** {@inheritDoc} */
    @Override
    public void setType(String newType) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearError(Errors error) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearOk(Ok ok) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearWarning(Warning warning) {}

    /** {@inheritDoc} */
    @Override
    public void infoPlayerParam(double allowMultDefaultType, double dashPowerRateDeltaMax,
                                double dashPowerRateDeltaMin, double effortMaxDeltaFactor, double effortMinDeltaFactor,
                                double extraStaminaDeltaMax, double extraStaminaDeltaMin,
                                double inertiaMomentDeltaFactor, double kickRandDeltaFactor,
                                double kickableMarginDeltaMax, double kickableMarginDeltaMin,
                                double newDashPowerRateDeltaMax, double newDashPowerRateDeltaMin,
                                double newStaminaIncMaxDeltaFactor, double playerDecayDeltaMax,
                                double playerDecayDeltaMin, double playerTypes, double ptMax, double randomSeed,
                                double staminaIncMaxDeltaFactor, double subsMax) {}

    /** {@inheritDoc} */
    @Override
    public void infoPlayerType(int id, double playerSpeedMax, double staminaIncMax, double playerDecay,
                               double inertiaMoment, double dashPowerRate, double playerSize, double kickableMargin,
                               double kickRand, double extraStamina, double effortMax, double effortMin) {}

    /** {@inheritDoc} */
    @Override
    public void infoCPTOther(int unum) {}

    /** {@inheritDoc} */
    @Override
    public void infoCPTOwn(int unum, int type) {}

    /** {@inheritDoc} */
    @Override
    public void infoServerParam(HashMap<ServerParams, Object> info) {}

    /**
     * This is the action performed when the player can see the ball.
     * It involves running at it and kicking it...
     */
    private void canSeeBallAction() {
        switch (this.getPlayer().getNumber()) {
                case 1 :     
                    if (log.isDebugEnabled()) {
                        log.debug("b(" + directionBall + "," + distanceBall + ")");
                    }
                    break;
                case 2 : case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:
//                    if (jgMakeRunForBall == true) {
//                       // this.getPlayer().say("going for ball");
//                    }
//                    //If the player has the ball kick it at a teammate
//                    //Else determine whether to run for the ball or let someone else keep it
//                    if (jginPossession == true) {
//                        log.info("kick at teammate");
//                        jgkickAtTeamMate();
//                    } else {
                        //Find out if someone has the ball
                        //if noone has the ball turn to it else run into space
                        if (jgInPossession == true) {
                            jgFinalTurnDirection = directionBall;
                            if (jgCanSeeOpponentGoal && jgDirectionOpponentGoal <= 40.0) {
                                log.info("take a shot at goal");
                                jgKickAtGoal();
                            } else {
                                log.info("kick at teammate");
                                jgKickAtTeamMate();                                
                            }
                        } else if (jgInPossession == false && jgPlayerWithTheBall == null) {
                            jgTurnTowardBall();
                            log.info("run toward ball");
                        } else {
                            jgRunIntoSpace();
                            log.info("run into space");
                        }
//                    }
                    break;
                default :
                    throw new Error("number must be initialized before move");
            }
    }

    /**
     * If the player can see anything that is not a ball or a goal, it turns.
     */
    private void canSeeAnythingAction() {
         switch (this.getPlayer().getNumber()) {
            case 1 :     
                if (log.isDebugEnabled()) {
                log.debug("a"); }
                break;
            case 2 : case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:
                log.debug("Can see anything action");
                //Will run the player into space, if it has to avoid players it avoids, else it turns to the ball
                if (directionBall != 0.0 && jgRunIntoSpace() == false) {
                    jgFinalTurnDirection = directionBall;                    
                } else if (jgRunIntoSpace() == false) {
                    //If theres noone to avoid then turn to ball
                    if (canSeeBall) {
                        jgTurnTowardBall();
                    } else {
                        jgTurnToPosition();
                    }
                }
                break;
            default :
                throw new Error("number must be initialized before move");
        }
    }

    /**
     * If the player can see nothing, it turns 180 degrees.
     */
    private void canSeeNothingAction() {
        switch (this.getPlayer().getNumber()) {
                case 1 : 
                    if (log.isDebugEnabled()) {
                        log.debug("n");
                    }
                    break;
                case 2 : case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:
                    //Attacker 1, if player sees nothing it will turn 180
                    jgFinalTurnDirection = 180;
                    break;
                default :
                    throw new Error("number must be initialized before move");
            }
    }

    /**
     * If the player can see its own goal, it goes and stands by it...
     */
    private void canSeeOwnGoalAction() {
        switch (this.getPlayer().getNumber()) {
                case 1 :     
                    //getPlayer().dash(this.randomDashValueFast());
                    //turnTowardOwnGoal();
                    if (log.isDebugEnabled()) {
                        log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
                    }
                    break;
                case 2 : case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:
//                    if (jgPlayerDoesHaveBall() == true && jgCanSeeOpponentGoal == true) {
//                        jgfinalTurnDirection = jgDirectionOpponentGoal;
//                        jgfinalKickDirection = -180;
//                        jgfinalKickPower = 50;
//                    } else {
//                        //Attacker does not go near goal so will run out to midfield
                        if (jgCanSeeOpponentGoal) {
                            jgturnTowardOpponentGoal();
                        } else {
                            jgFinalTurnDirection = 180;
                        }
//                    }
                    break;
                default :
                    throw new Error("number must be initialized before move");
            }
    }

    /**
     * Randomly choose a fast dash value.
     * @return
     */
    private int randomDashValueFast() {
        return 30 + random.nextInt(100);
    }

    /**
     * Randomly choose a slow dash value.
     * @return
     */
    private int randomDashValueSlow() {
        return -10 + random.nextInt(50);
    }

    /**
     * Turn towards the ball.
     */
    private void jgTurnTowardBall() {
        jgFinalTurnDirection = directionBall;
    }

    //Determines who has the ball and returns false if noone has the ball
    private boolean jgPlayerDoesHaveBall() {
        for (int i = 0; i < jgAllPlayers.size(); i++) {
            SeenPlayer player = jgAllPlayers.get(i);
            log.info(player.distanceFromBall);
            if (player.distanceFromBall < jgPOSSESSIONDISTANCE) {
                log.info("true");
                jgPlayerWithTheBall = player;
                return true;
            }
        }
        return false;                            
    }

    /**
     * Turn towards our goal.
     */
    private void turnTowardOwnGoal() {
        getPlayer().turn(directionOwnGoal);
    }

    /**
     * Turn towards our goal.
     */
    private void jgturnTowardOpponentGoal() {
        this.getPlayer().turn(jgDirectionOpponentGoal);
    }

    /**
     * Randomly choose a kick direction.
     * @return
     */
    private int randomKickDirectionValue() {
        return -45 + random.nextInt(90);
    }
    
    private void jgdibbleBall() {
        this.jgFinalKickPower = 5;
        this.jgFinalKickDirection = 0;
    }
    
    private void jgKickAtTeamMate() {
        boolean kicked = false;
        //Will kick towards the first player over a certain distance
        for (int i = 0; i < jgAllPlayers.size(); i++) {
            SeenPlayer player = jgAllPlayers.get(i);
            if (player.distance > jgPOSSESSIONDISTANCE && player.isTeammate) {
                //Cast double to int for kick distance
                Double d = player.distance; Integer in = d.intValue();
                
                this.jgFinalKickPower = in;
                this.jgFinalKickDirection = player.direction; 
                kicked = true;
                log.info("Ball was kicked to teammate");
                break;
            }
        }
        //If the ball hasn't been kicked then the player can't see any other useful players
        //Therefore they will turn to the opponent goal using a series of kicks and run with the ball
        if (!kicked) {
            log.info("Ball not kicked, turn to op goal");
            if (jgCanSeeOpponentGoal) {
                if (jgDirectionOpponentGoal < -25.0 || jgDirectionOpponentGoal > 25.0) {
                    //Turn player towards opponents goal slowly to avoid loosing the ball
                    this.jgFinalKickPower = 5;
                    this.jgFinalKickDirection = 45; 
                } else {
                    jgFinalTurnDirection = jgDirectionOpponentGoal;
                    jgdibbleBall();
                }
            } else {
                //Turn player towards opponents goal slowly to avoid loosing the ball
                this.jgFinalKickPower = 5;
                this.jgFinalKickDirection = 45; 
            }
        }
    }

    /**
     * Pause the thread.
     * @param ms How long to pause the thread for (in ms).
     */
    private synchronized void pause(int ms) {
        try {
            this.wait(ms);
        } catch (InterruptedException ex) {
            log.warn("Interrupted Exception ", ex);
        }
    }
    
    //Will run the player into space avoiding players returns true if player had to be avoided
    //Determines which team has the ball and decides whether to retreat or attack
    private boolean jgRunIntoSpace() {
        Boolean status = false;
        for (int i = 0; i < jgAllPlayers.size(); i++) {
            SeenPlayer player = jgAllPlayers.get(i);
            log.info(player.number + " :Number, Distance: " + player.distance);
            if (player.distance < 5.0 && player.direction > -15 && player.direction < 15) {
                jgFinalTurnDirection = player.direction + 90;
                status = true;
            }        
        }
        if (!jgUpdatedThePlayerList) {
            //If the player list hasn't been updated then something is wrong so turn around
            jgFinalTurnDirection = 180;
        }  
        return status;
    }
    
    //Used to determine the place that the player will go to if they cannot see the ball or their own goal
    private void jgTurnToPosition() {
        //If the opponents goal can be seen then run forward if not offside, else retreat
        //If the player cannot see an opponent it is likely offside so will turn around
        if (jgCanSeeOpponentGoal) {
            if (jgUpdatedThePlayerList == false) {
                jgFinalTurnDirection = 180;
            } else {
                jgFinalTurnDirection = jgDirectionOpponentGoal;
            }
        } else {
            jgFinalRunPower = 60;
            jgRunIntoSpace();
        }
    }

    //Finds the closest person to the ball, if noone else is visible then the current player assumes control
    private void jgFindClosestPlayerToBall() {
        for (int i = 0; i < jgAllPlayers.size(); i++) {
            SeenPlayer player = jgAllPlayers.get(i);
            if (player.distanceFromBall < jgPlayerWithTheBall.distanceFromBall) {
                jgPlayerWithTheBall = player;
            }
        }
        if (canSeeBall && jgPlayerWithTheBall != null) {
            if (this.distanceBall < jgPlayerWithTheBall.distanceFromBall) {
                jgInPossession = true;
            }
        } else if (canSeeBall) {
            jgInPossession = true;
        } else {
            jgInPossession = false;
        }
    }

    //Assumes that you can see the opponent goal and is within a certain range
    private void jgKickAtGoal() {
        //Kicks ball at goal with a variance of 10 degrees
        jgFinalKickDirection = jgDirectionOpponentGoal - 5 + Math.random() * 10;
        jgFinalKickPower = 50;
    }
}
//Storage of the seen player inside memory, just returns all of the values
class SeenPlayer {
    
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
    public double distanceFromBall;

    public SeenPlayer (int num, boolean goal, double dist, double dir, double distC,
                                 double dirC, double bod, double head, boolean teamMate) {
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
    }
    
    //Takes distance to the ball, then the player, then the angle the ball is at and then angle that the other player is at relative to the player
    public void calculateDistance(double distanceA, double distanceB, double angleA, double angleB) {
        //Calulate the angle that the ball/player is at relative
        double angleC = Math.abs(angleA) - Math.abs(angleB);
        log.info("C:" + angleC + " B:" + angleB + " A:" + angleA);
        //Calculates distance of player from ball based on cosine
        //Based on rule here: http://www.mathstat.strath.ac.uk/basicmaths/332_sineandcosinerules.html
        double cosSq = Math.pow(distanceA, 2) + Math.pow(distanceB, 2) - ((2 * (distanceA*distanceB)) * Math.cos(Math.abs(angleC)));
        double result = Math.sqrt(cosSq);
        distanceFromBall = result;
    }
    
}