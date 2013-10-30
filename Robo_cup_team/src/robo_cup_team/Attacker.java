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
import java.lang.reflect.Field;

import org.apache.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import robo_cup_team.SeenPlayer;

/**
 * A Attacker controller.
 *
 * @author Atan
 */
public class Attacker implements ControllerPlayer {
    private static int    count         = 0;
    private static Logger log           = Logger.getLogger(Attacker.class);
    private Random        random        = null;
    private boolean       canSeeOwnGoal = false;
    private boolean       canSeeOpponentGoal = false;
    private boolean       canSeeNothing = true;
    private boolean       canSeeBall    = false;
    private double        directionBall;
    private double        directionOwnGoal;
    private double        directionOpponentGoal;
    private double        distanceBall = 100.0;
    private double        distanceOwnGoal;
    private double        distanceOpponentGoal;
    private ActionsPlayer player;
    private List<SeenPlayer> allPlayers = new ArrayList<>();
    private SeenPlayer    closestPlayer;    
    private SeenPlayer    closestOtherPlayer;
    static final int      WALKSPEED = 20;
    static final int      JOGSPEED = 50;
    static final int      RUNSPEED = 70;
    static final int      SPRINTSPEED = 100;
    private boolean       hasBall;
    private boolean       contestedBall;
    static final double   POSSESSIONDISTANCE = 0.2;
    /**
     * Constructs a new simple client.
     */
    public Attacker() {
        random = new Random(System.currentTimeMillis() + count);
        count++;
    }

    /** {@inheritDoc}
     * @return  */
    @Override
    public ActionsPlayer getPlayer() {
        return player;
    }

    /** {@inheritDoc}
     * @param p */
    @Override
    public void setPlayer(ActionsPlayer p) {
        player = p;
    }

    /** {@inheritDoc} */
    @Override
    //Runs before all the visual information has been processed
    //Essentially forces the ai to re-evaluate every variable on every new refresh
    public void preInfo() {
        canSeeOwnGoal = false;
        canSeeBall    = false;
        canSeeNothing = true;
        hasBall = false;
        contestedBall = false;
    }

    /** {@inheritDoc} */
    //Runs after all the visual information has been processed
    @Override
    public void postInfo() {
        //Evaluate all known data
        evaluateData();
        //Determine the action to make if has/doesn't have ball
        //Also determine the direction to travel

        if (closestPlayer.distance <= POSSESSIONDISTANCE || closestOtherPlayer.distance <= POSSESSIONDISTANCE) {
            if (closestOtherPlayer.distance <= POSSESSIONDISTANCE) {
                getPlayer().turn(closestOtherPlayer.direction + 180);                    
            } else {
                getPlayer().turn(closestPlayer.direction + 180);
            }
            getPlayer().dash(WALKSPEED);
        } else {
            if(canSeeBall) {
                if (distanceBall <= POSSESSIONDISTANCE) {
                    actionWithBall();
                } else {
                    actionWithoutBall();
                }
            } else {
                cantSeeTheBall();
            }
        }
    }
    
    private void actionWithBall() {
        if(closestPlayer != null && closestOtherPlayer.distance < POSSESSIONDISTANCE + 1) {
            //The ball is contested, kick towards teammate
            contestedBall = true;
            hasBall = true;
            kickAtTeamMate();
        } else {
            hasBall = true;
            dribbleWithBall();
        }
    }
    
    private void actionWithoutBall() {
        boolean ourPlayerHasTheBall = false;
        boolean otherPlayerHasTheBall = false;
        for (int i = 0; i < allPlayers.size(); i++) {
            SeenPlayer seen = allPlayers.get(i);
            
            log.debug(seen.number);
            log.debug(seen.hasBall);
            if (seen.hasBall) {
                if (seen.isTeammate) {
                    ourPlayerHasTheBall = true;
                } else {
                    otherPlayerHasTheBall = true;
                }
            }
        }
        if (ourPlayerHasTheBall) {
            getPlayer().turn(directionOpponentGoal);
        } else if (otherPlayerHasTheBall) {
            getPlayer().turn(directionOwnGoal);
        } else if (ourPlayerHasTheBall == false && otherPlayerHasTheBall == false) {
            getPlayer().turn(directionBall);
        }
    }

    private void cantSeeTheBall() {
        getPlayer().turn(90);
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
        if (flag == Flag.CENTER) {
            this.canSeeOpponentGoal = true;
            this.distanceOpponentGoal = distance;
            this.directionOpponentGoal = direction;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeLine(Line line, double distance, double direction, double distChange, double dirChange,
                            double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOther(int number, boolean goalie, double distance, double direction, double distChange,
                                   double dirChange, double bodyFacingDirection, double headFacingDirection) {
        allPlayers.clear();
        SeenPlayer seenPlayer = new SeenPlayer(number, goalie, distance, direction, distChange,
                                 dirChange, bodyFacingDirection, headFacingDirection, false, distanceBall, directionBall);
        allPlayers.add(seenPlayer);
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange,
                                 double dirChange, double bodyFacingDirection, double headFacingDirection) {
        allPlayers.clear();
        SeenPlayer seenPlayer = new SeenPlayer(number, goalie, distance, direction, distChange,
                                 dirChange, bodyFacingDirection, headFacingDirection, true, distanceBall, directionBall);
        allPlayers.add(seenPlayer);
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeBall(double distance, double direction, double distChange, double dirChange,
                            double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing      = false;
        this.canSeeBall    = true;
        this.distanceBall  = distance;
        this.directionBall = direction;
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
                    this.getPlayer().move(-10, 0);
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
                    this.getPlayer().move(-20, 10);
                    break;
                case 6 :
                    this.getPlayer().move(-20, -10);
                    break;
                case 7 :
                    this.getPlayer().move(-20, 20);
                    break;
                case 8 :
                    this.getPlayer().move(-20, -20);
                    break;
                case 9 :
                    this.getPlayer().move(-30, 0);
                    break;
                case 10 :
                    this.getPlayer().move(-40, 10);
                    break;
                case 11 :
                    this.getPlayer().move(-40, -10);
                    break;
                default :
                    throw new Error("number must be initialized before move");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoHearPlayer(double direction, String message) {}

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
        getPlayer().dash(this.randomDashValueFast());
        turnTowardBall();
        //Will only take the ball if its close
        //Keep the ball if it is close
        if (distanceBall < 0.7) {
            getPlayer().kick(1, randomKickDirectionValue());
        } else {
            //Kick the ball to teamate if they are close
            for (int i = 0; i < allPlayers.size(); i++) {
                SeenPlayer s = allPlayers.get(i);
                if (s.distance < 20) {
                    turnTowardPlayer(s.direction);
                    getPlayer().kick(50, 0.0);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("b(" + directionBall + "," + distanceBall + ")");
        }
    }

    /**
     * If the player can see anything that is not a ball or a goal, it turns.
     */
    private void canSeeAnythingAction() {
        getPlayer().dash(this.randomDashValueSlow());
        getPlayer().turn(20);
        if (log.isDebugEnabled()) {
            log.debug("a");
        }
    }

    /**
     * If the player can see nothing, it runs forward
     */
    private void canSeeNothingAction() {
        
        getPlayer().dash(0);
        if (log.isDebugEnabled()) {
            log.debug("n");
            log.info("Im blind here");
        }
    }

    private void evaluateData() {
        //Will calulate the closest player to the player for own and other players
        for (int i = 0; i < allPlayers.size() - 1; i++) {
            SeenPlayer player = allPlayers.get(i);
            closestPlayer = player;
            closestOtherPlayer = player;
            if (player.distance < closestPlayer.distance) {
                closestPlayer = player;
            }
            if (player.distance < closestOtherPlayer.distance) {
                closestOtherPlayer = player;
            }
            //If within certain bounds then the player has the ball
            log.debug(player.realDistanceBall);
            if (player.realDistanceBall == POSSESSIONDISTANCE) {
                player.hasBall(true);
            }
        }
    }
    
    /**
     * If the player can see its own goal, it goes and stands by it...
     */
    private void canSeeOwnGoalAction() {
        getPlayer().dash(this.randomDashValueFast());
        turnTowardOwnGoal();
        if (log.isDebugEnabled()) {
            log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
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
        getPlayer().turn(directionBall);
    }
    
    //Run towards the goal
    private void turnTowardGoal() {
        getPlayer().turn(directionOpponentGoal);
    }

    /**
     * Turn towards our goal.
     */
    private void turnTowardOwnGoal() {
        getPlayer().turn(directionOwnGoal);
    }
    
    private void turnTowardPlayer(double direction) {
        getPlayer().turn(direction);
    }

    //Kicks the ball in forward direction
    private void dribbleWithBall() {
        getPlayer().turn(directionOpponentGoal);
        getPlayer().kick(10, 0);
    }
    
    /**
     * Randomly choose a kick direction.
     * @return
     */
    private int randomKickDirectionValue() {
        return -45 + random.nextInt(90);
    }

    //Moves the player into free space avoiding opponents
    private void moveForwardIntoSpace(int speed) {
        getPlayer().dash(speed);
        log.info("Player number: " + getPlayer().getNumber());
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

    private void kickAtTeamMate() {
        getPlayer().turn(closestPlayer.direction);
        getPlayer().kick(50, 0);
    }
}
