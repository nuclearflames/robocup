
//~--- non-JDK imports --------------------------------------------------------

import com.github.robocup_atan.atan.model.AbstractTeam;
import com.github.robocup_atan.atan.model.ControllerCoach;
import com.github.robocup_atan.atan.model.ControllerPlayer;
import robo_cup_team.Simple;

/**
 * A class to setup a Simple Silly AbstractTeam.
 *
 * @author Atan
 */
public class SimplySillyTeam extends AbstractTeam {

    /**
     * Constructs a new simple silly team.
     *
     * @param name The team name.
     * @param port The port to connect to SServer.
     * @param hostname The SServer hostname.
     * @param hasCoach True if connecting a coach.
     */
    public SimplySillyTeam(String name, int port, String hostname, boolean hasCoach) {
        super(name, port, hostname, hasCoach);
    }

    /**
     * {@inheritDoc}
     *
     * The first controller of the team is silly the others are simple.
     */
    @Override
    public ControllerPlayer getNewControllerPlayer(int number) {
        //This class determines what type of player the object is
        //Setup the team based initally on the 4-4-2 formation
        //var number is in array notation, the player numbers are + 1
        //number = 0 is the goalkeeper
        //number = 1
        return new Simple();
    }

    /**
     * {@inheritDoc}
     *
     * Generates a new coach.
     */
    @Override
    public ControllerCoach getNewControllerCoach() {
        return new Coach();
    }
}
