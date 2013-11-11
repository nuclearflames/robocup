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
    private List<SeenPlayer> allPlayers = new ArrayList<>();
    
    static final int      jgWALKSPEED = 20;
    static final int      jgJOGSPEED = 50;
    static final int      jgRUNSPEED = 70;
    static final int      jgSPRINTSPEED = 100;
    static double         jgPOSSESSIONDISTANCE = 1.0;
    
    private double        jgfinalKickDirection = 0.0;
    private int           jgfinalKickPower = 0;
    private int           jgfinalRunPower = 60;
    private double        jgfinalTurnDirection = 0;
    private boolean       jgplayOn = false;
    private boolean       jgrunForBall = true;
    private boolean       jgcanSeeOpponentGoal = false;
    private boolean       jginPossession = false;
    private double        jgdirectionOpponentGoal;
    
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
        jginPossession = false;
        allPlayers.clear();
    
        jgfinalKickDirection = 0.0;
        jgfinalKickPower = 0;
        jgfinalRunPower = 60;
        jgfinalTurnDirection = 0;
        jgrunForBall = false;
    }

    /** {@inheritDoc} */
    @Override
    public void postInfo() {
        switch (this.getPlayer().getNumber()) {
            case 2:
            case 3:
                if (canSeeNothing) {
                    log.info("Can see nothing");
                    canSeeNothingAction();
                } else if (canSeeOwnGoal) {
                    if ((distanceOwnGoal < 40) && (distanceOwnGoal > 10)) {
                        log.info("Can see own goal");
                        canSeeOwnGoalAction();
                    } else if (canSeeBall) {
                        log.info("Can see ball");
                        canSeeBallAction();
                    } else {
                        log.info("Can see Anything");
                        canSeeAnythingAction();
                    }
                } else if (canSeeBall) {
                    log.info("Can see Ball");
                    canSeeBallAction();
                } else {
                    log.info("Can see Anything");
                    canSeeAnythingAction();
                }
                if (jgfinalRunPower != 0.0) {
                    if (this.jgplayOn == true) {
                        getPlayer().dash(jgfinalRunPower);                        
                    } else if (this.jgplayOn == false) {
                        if (this.distanceBall > jgPOSSESSIONDISTANCE) {
                            getPlayer().dash(jgfinalRunPower);                            
                        }                        
                    }
                }
                if (jgfinalTurnDirection != 0) {
                    getPlayer().turn(jgfinalTurnDirection);
                }
                if (jgfinalKickPower != 0 && jgplayOn) {
                    getPlayer().kick(jgfinalKickPower, jgfinalKickDirection);
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
        jgcanSeeOpponentGoal = true;
        jgdirectionOpponentGoal = direction;
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
        getPlayer().say("going for ball");
        log.info(distance + ", " + jgPOSSESSIONDISTANCE); 
        if (distance < jgPOSSESSIONDISTANCE) {
            log.info("inPossession"); 
            this.jginPossession = true;
        } else {
            this.jginPossession = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOther(int number, boolean goalie, double distance, double direction, double distChange,
                                   double dirChange, double bodyFacingDirection, double headFacingDirection) {
        allPlayers.clear();
        SeenPlayer seenPlayer = new SeenPlayer(number, goalie, distance, direction, distChange,
                                 dirChange, bodyFacingDirection, headFacingDirection, false);
        if (canSeeBall == true) {
            seenPlayer.calculateDistance(distanceBall, distance, directionBall, direction);
        } else {
            seenPlayer.distanceFromBall = 1000;
        }
        allPlayers.add(seenPlayer);
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange,
                                 double dirChange, double bodyFacingDirection, double headFacingDirection) {
        allPlayers.clear();
        SeenPlayer seenPlayer = new SeenPlayer(number, goalie, distance, direction, distChange,
                                 dirChange, bodyFacingDirection, headFacingDirection, true);
        if (canSeeBall == true) {
            seenPlayer.calculateDistance(distanceBall, distance, directionBall, direction);
        } else {
            seenPlayer.distanceFromBall = 1000;
        }
        allPlayers.add(seenPlayer);
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
                case 2 :
                case 3 :
                    this.jgplayOn = true;                     
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
        } else if (playMode != PlayMode.PLAY_ON) {
                this.jgplayOn = false; 
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoHearPlayer(double direction, String message) {
            switch (this.getPlayer().getNumber()) {
                case 1 :
                    this.getPlayer().move(-50, 0);                 
                    break;
                case 2 :
                case 3 :
                    if (message == "going for ball") {
                        this.jgrunForBall = false;
                    }                     
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
                case 2 :
                case 3 :
                    //If the player has the ball kick it at a teammate
                    //Else determine whether to run for the ball or let someone else keep it
                    if (jginPossession == true) {
                        log.info("kick at teammate");
                        kickAtTeamMate();
                    } else {
                        //if noone has the ball turn to it else run into space
                        if (doesHaveBall() == false && jgrunForBall == true) {
                            log.info("Turned toward ball");
                            turnTowardBall();
                        } else {
                            if (runIntoSpace() == false) {
                                log.info("Turned into space");
                            }
                        }
                    }
                    break;
                case 4 :
                    getPlayer().dash(this.randomDashValueFast());
                    turnTowardBall();
                    if (distanceBall < 0.7) {
                        getPlayer().kick(50, randomKickDirectionValue());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("b(" + directionBall + "," + distanceBall + ")");
                    }
                    break;
                case 5 :
                    getPlayer().dash(this.randomDashValueFast());
                    turnTowardBall();
                    if (distanceBall < 0.7) {
                        getPlayer().kick(50, randomKickDirectionValue());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("b(" + directionBall + "," + distanceBall + ")");
                    }
                    break;
                case 6 :
                    getPlayer().dash(this.randomDashValueFast());
                    turnTowardBall();
                    if (distanceBall < 0.7) {
                        getPlayer().kick(50, randomKickDirectionValue());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("b(" + directionBall + "," + distanceBall + ")");
                    }
                    break;
                case 7 :
                    getPlayer().dash(this.randomDashValueFast());
                    turnTowardBall();
                    if (distanceBall < 0.7) {
                        getPlayer().kick(50, randomKickDirectionValue());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("b(" + directionBall + "," + distanceBall + ")");
                    }
                    break;
                case 8 :
                    getPlayer().dash(this.randomDashValueFast());
                    turnTowardBall();
                    if (distanceBall < 0.7) {
                        getPlayer().kick(50, randomKickDirectionValue());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("b(" + directionBall + "," + distanceBall + ")");
                    }
                    break;
                case 9 :
                    getPlayer().dash(this.randomDashValueFast());
                    turnTowardBall();
                    if (distanceBall < 0.7) {
                        getPlayer().kick(50, randomKickDirectionValue());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("b(" + directionBall + "," + distanceBall + ")");
                    }
                    break;
                case 10 :
                    getPlayer().dash(this.randomDashValueFast());
                    turnTowardBall();
                    if (distanceBall < 0.7) {
                        getPlayer().kick(50, randomKickDirectionValue());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("b(" + directionBall + "," + distanceBall + ")");
                    }
                    break;
                case 11 :
                    getPlayer().dash(this.randomDashValueFast());
                    turnTowardBall();
                    if (distanceBall < 0.7) {
                        getPlayer().kick(50, randomKickDirectionValue());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("b(" + directionBall + "," + distanceBall + ")");
                    }
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
                case 2 :
                case 3 :
                    log.debug("Can see anything action");
                    //Will run the player into space if it has to avoid players it avoids, else it turns to the ball
                    if (runIntoSpace() == false) {
                        //If theres noone to avoid then turn to ball
                        if (canSeeBall) {
                            turnTowardBall();
                        } else {
                            turnAround();
                        }
                    }
                    break;
                case 4 :
                    getPlayer().dash(this.randomDashValueSlow());
                    getPlayer().turn(20);
                    if (log.isDebugEnabled()) {
                    log.debug("a"); }                     
                    break;
                case 5 :
                    getPlayer().dash(this.randomDashValueSlow());
                    getPlayer().turn(20);
                    if (log.isDebugEnabled()) {
                    log.debug("a"); }                     
                    break;
                case 6 :
                    getPlayer().dash(this.randomDashValueSlow());
                    getPlayer().turn(20);
                    if (log.isDebugEnabled()) {
                    log.debug("a"); }                     
                    break;
                case 7 :
                    getPlayer().dash(this.randomDashValueSlow());
                    getPlayer().turn(20);
                    if (log.isDebugEnabled()) {
                    log.debug("a"); }                     
                    break;
                case 8 :
                    getPlayer().dash(this.randomDashValueSlow());
                    getPlayer().turn(20);
                    if (log.isDebugEnabled()) {
                    log.debug("a"); }                     
                    break;
                case 9 :
                    getPlayer().dash(this.randomDashValueSlow());
                    getPlayer().turn(20);
                    if (log.isDebugEnabled()) {
                    log.debug("a"); }                     
                    break;
                case 10 :
                    getPlayer().dash(this.randomDashValueSlow());
                    getPlayer().turn(20);
                    if (log.isDebugEnabled()) {
                    log.debug("a"); }                     
                    break;
                case 11 :
                    getPlayer().dash(this.randomDashValueSlow());
                    getPlayer().turn(20);
                    if (log.isDebugEnabled()) {
                    log.debug("a"); }                     
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
                case 2 :
                case 3 :
                    //Attacker 1, if player sees nothing it will turn 180
                    jgfinalTurnDirection = 180;
                    break;
                case 4 :
                    turnTowardBall();
                    if (log.isDebugEnabled()) {
                        log.debug("n");
                    }
                    break;
                case 5 :
                    turnTowardBall();
                    if (log.isDebugEnabled()) {
                        log.debug("n");
                    }
                    break;
                case 6 :
                    turnTowardBall();
                    if (log.isDebugEnabled()) {
                        log.debug("n");
                    }
                    break;
                case 7 :
                    turnTowardBall();
                    getPlayer().dash(this.randomDashValueSlow());
                    getPlayer().turn(20);
                    if (log.isDebugEnabled()) {
                    log.debug("a"); }                     
                    break;
                case 8 :
                    turnTowardBall();
                    if (log.isDebugEnabled()) {
                        log.debug("n");
                    }
                    break;
                case 9 :
                    turnTowardBall();
                    if (log.isDebugEnabled()) {
                        log.debug("n");
                    }
                    break;
                case 10 :
                    turnTowardBall();
                    if (log.isDebugEnabled()) {
                        log.debug("n");
                    }
                    break;
                case 11 :
                    turnTowardBall();
                    if (log.isDebugEnabled()) {
                        log.debug("n");
                    }
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
                case 2 :
                case 3 :
                    //Attacker does not go near goal so will run out to midfield
                    if (jgcanSeeOpponentGoal) {
                        turnTowardOpponentGoal();
                    } else {
                        jgfinalTurnDirection = 180;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
                    }
                    break;
                case 4 :
                    //getPlayer().dash(this.randomDashValueFast());
                    //turnTowardOwnGoal();
                    if (log.isDebugEnabled()) {
                        log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
                    }
                    break;
                case 5 :
                    //getPlayer().dash(this.randomDashValueFast());
                    //turnTowardOwnGoal();
                    if (log.isDebugEnabled()) {
                        log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
                    }
                    break;
                case 6 :
                    //getPlayer().dash(this.randomDashValueFast());
                    //turnTowardOwnGoal();
                    if (log.isDebugEnabled()) {
                        log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
                    }
                    break;
                case 7 :
                    //getPlayer().dash(this.randomDashValueFast());
                    //turnTowardOwnGoal();
                    if (log.isDebugEnabled()) {
                        log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
                    }
                    break;
                case 8 :
                    //getPlayer().dash(this.randomDashValueFast());
                    //turnTowardOwnGoal();
                    if (log.isDebugEnabled()) {
                        log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
                    }
                    break;
                case 9 :
                    //getPlayer().dash(this.randomDashValueFast());
                    //turnTowardOwnGoal();
                    if (log.isDebugEnabled()) {
                        log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
                    }
                    break;
                case 10 :
                    //getPlayer().dash(this.randomDashValueFast());
                    //turnTowardOwnGoal();
                    if (log.isDebugEnabled()) {
                        log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
                    }
                    break;
                case 11 :
                    //getPlayer().dash(this.randomDashValueFast());
                    //turnTowardOwnGoal();
                    if (log.isDebugEnabled()) {
                        log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
                    }
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
    private void turnTowardBall() {
        jgfinalTurnDirection = directionBall;
    }

                        
    private boolean doesHaveBall() {
        for (int i = 0; i < allPlayers.size(); i++) {
            SeenPlayer player = allPlayers.get(i);
            log.info(player.distanceFromBall);
            if (player.distanceFromBall < 5.0) {
                log.info("true");
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
    private void turnTowardOpponentGoal() {
        getPlayer().turn(jgdirectionOpponentGoal);
    }

    /**
     * Randomly choose a kick direction.
     * @return
     */
    private int randomKickDirectionValue() {
        return -45 + random.nextInt(90);
    }
    
    private void dibbleBall() {
        this.jgfinalKickPower = 5;
        this.jgfinalKickDirection = 0;
    }
    
    private void kickAtTeamMate() {
        boolean kicked = false;
        //Will kick towards the first player over a certain distance
        for (int i = 0; i < allPlayers.size(); i++) {
            SeenPlayer player = allPlayers.get(i);
            if (player.distance > jgPOSSESSIONDISTANCE) {
                //Cast double to int for kick distance
                Double d = player.distance; Integer in = d.intValue();
                
                this.jgfinalKickPower = in;
                this.jgfinalKickDirection = player.direction; 
                kicked = true;
                log.info("Ball was kicked to teammate");
            }
        }
        //If the ball hasn't been kicked then the player can't see any other useful players
        //Therefore they will turn to the opponent goal using a series of kicks and run with the ball
        if (!kicked) {
            log.info("Ball not kicked, turn to op goal");
            if (jgcanSeeOpponentGoal) {
                if (jgdirectionOpponentGoal < -25.0 || jgdirectionOpponentGoal > 25.0) {
                    //Turn player towards opponents goal slowly to avoid loosing the ball
                    this.jgfinalKickPower = 3;
                    this.jgfinalKickDirection = 20; 
                } else {
                    jgfinalTurnDirection = jgdirectionOpponentGoal;
                    dibbleBall();
                }
            } else {
                //Turn player towards opponents goal slowly to avoid loosing the ball
                this.jgfinalKickPower = 3;
                this.jgfinalKickDirection = 20; 
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
    private boolean runIntoSpace() {
        Boolean status = false;
        for (int i = 0; i < allPlayers.size(); i++) {
            SeenPlayer player = allPlayers.get(i);
            if (player.direction > -15 && player.direction < 15 && player.distance < 3.0) {
                if (player.direction > -15) {
                    jgfinalTurnDirection = player.direction + 45;                    
                } else {
                    jgfinalTurnDirection = player.direction - 45;
                }
                status = true;
            }
        }
        return status;
    }

    private void turnAround() {
        getPlayer().turn(90);
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
        double angleC = angleA - angleB;
        //Calculates distance of player from ball based on cosine
        //Based on rule here: http://www.mathstat.strath.ac.uk/basicmaths/332_sineandcosinerules.html
        double cosSq = Math.pow(distanceA, 2) + Math.pow(distanceB, 2) - ((2 * (distanceA*distanceB)) * Math.cos(angleC));
        double result = Math.round(Math.sqrt(cosSq));
        distanceFromBall = result;
    }
    
}