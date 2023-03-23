import javax.swing.*;

import java.io.IOException;
import java.lang.reflect.*;

/**
 * The main class that parses command line parameters and initializes the Othello game.
 *
 * @author Mai Ajspur, Kevin Tierney
 * @version 9.2.2018
 */
public class Othello
{
    record Pair(IOthelloAI ai1, IOthelloAI ai2) {}
    public static String HUMAN_CMD = "human";
    public static boolean restart = false;
	
	/**
     * Valid arguments: ai1 ai2 size 
     * Standard values for size (length of square board) is 8
     */
    public static void main(String[] args)
    {
        IOthelloAI ai1 = null;
        IOthelloAI ai2 = null;
        int size = 8;
        
        boolean err = args.length < 2;
        String errMsg = "You need to supply at least two arguments";

        if (args.length >= 2){
        	try {
                ai1 = parseGameLogicParam(args[0]);
                ai2 = parseGameLogicParam(args[1]);
                if ( ai2 == null ){
                	errMsg = "Only the first player can be human";
                	err = true;
                }
            } catch(ClassNotFoundException cnf) {
                errMsg = cnf.toString();
                err = true;
            } catch(NoSuchMethodException nsme) {
                errMsg = "Your GameInstance had no constructor.";
                err = true;
            } catch(InstantiationException ie) {
                errMsg = "Your GameInstance could not be instantiated.";
                err = true;
            } catch(IllegalAccessException iae) {
                errMsg = "Your GameInstance caused an illegal access exception.";
                err = true;
            } catch(InvocationTargetException ite) {
                errMsg = "Your GameInstance constructor threw an exception: " + ite.toString();
                err = true;
            }
        	
            if(!err && ai1 == null && ai2 == null) {
                errMsg = "Two human players not allowed.";
                err = true;
            }

            if(args.length >= 3) {
            	try {
            		size = Integer.parseInt(args[2]);
            	
            		if ( size < 4 || size%2 != 0 ){
            			errMsg = "Board size should be an even number greater than 2";
            		err = true;
            		}
            	
            	} catch(NumberFormatException nfe) {
            		errMsg = "Could not parse size value: " + args[2];
            		err = true;
            	}
            }
        }
        
        if(err) {
        	printHelp(errMsg);
           	System.exit(1);
        }

        Pair[] ais = new Pair[] {
                new Pair(new DumAI(), new DumAI()),
                new Pair(new DumAI(), new RandomAI()),
                new Pair(new DumAI(), new Tokens()),
                new Pair(new DumAI(), new Corners()),
                new Pair(new DumAI(), new Moves()),
                new Pair(new DumAI(), new CornersTokens()),
                new Pair(new DumAI(), new MovesTokens()),
                new Pair(new DumAI(), new CornersMovesTokens()),

                new Pair(new RandomAI(), new DumAI()),
                new Pair(new RandomAI(), new RandomAI()),
                new Pair(new RandomAI(), new Tokens()),
                new Pair(new RandomAI(), new Corners()),
                new Pair(new RandomAI(), new Moves()),
                new Pair(new RandomAI(), new CornersTokens()),
                new Pair(new RandomAI(), new MovesTokens()),
                new Pair(new RandomAI(), new CornersMovesTokens()),

                new Pair(new Tokens(), new DumAI()),
                new Pair(new Tokens(), new RandomAI()),
                new Pair(new Tokens(), new Tokens()),
                new Pair(new Tokens(), new Corners()),
                new Pair(new Tokens(), new Moves()),
                new Pair(new Tokens(), new CornersTokens()),
                new Pair(new Tokens(), new MovesTokens()),
                new Pair(new Tokens(), new CornersMovesTokens()),

                new Pair(new Corners(), new DumAI()),
                new Pair(new Corners(), new RandomAI()),
                new Pair(new Corners(), new Tokens()),
                new Pair(new Corners(), new Corners()),
                new Pair(new Corners(), new Moves()),
                new Pair(new Corners(), new CornersTokens()),
                new Pair(new Corners(), new MovesTokens()),
                new Pair(new Corners(), new CornersMovesTokens()),

                new Pair(new Moves(), new DumAI()),
                new Pair(new Moves(), new RandomAI()),
                new Pair(new Moves(), new Tokens()),
                new Pair(new Moves(), new Corners()),
                new Pair(new Moves(), new Moves()),
                new Pair(new Moves(), new CornersTokens()),
                new Pair(new Moves(), new MovesTokens()),
                new Pair(new Moves(), new CornersMovesTokens()),

                new Pair(new CornersTokens(), new DumAI()),
                new Pair(new CornersTokens(), new RandomAI()),
                new Pair(new CornersTokens(), new Tokens()),
                new Pair(new CornersTokens(), new Corners()),
                new Pair(new CornersTokens(), new Moves()),
                new Pair(new CornersTokens(), new CornersTokens()),
                new Pair(new CornersTokens(), new MovesTokens()),
                new Pair(new CornersTokens(), new CornersMovesTokens()),

                new Pair(new MovesTokens(), new DumAI()),
                new Pair(new MovesTokens(), new RandomAI()),
                new Pair(new MovesTokens(), new Tokens()),
                new Pair(new MovesTokens(), new Corners()),
                new Pair(new MovesTokens(), new Moves()),
                new Pair(new MovesTokens(), new CornersTokens()),
                new Pair(new MovesTokens(), new MovesTokens()),
                new Pair(new MovesTokens(), new CornersMovesTokens()),

                new Pair(new CornersMovesTokens(), new DumAI()),
                new Pair(new CornersMovesTokens(), new RandomAI()),
                new Pair(new CornersMovesTokens(), new Tokens()),
                new Pair(new CornersMovesTokens(), new Corners()),
                new Pair(new CornersMovesTokens(), new Moves()),
                new Pair(new CornersMovesTokens(), new CornersTokens()),
                new Pair(new CornersMovesTokens(), new MovesTokens()),
                new Pair(new CornersMovesTokens(), new CornersMovesTokens()),
        };

        // Setup of the frame containing the game
        JFrame f = new JFrame();
        f.setSize((size + 2) * 100, (size + 2) * 100);
        f.setTitle("Othello");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        try{
            for (var pair : ais) {
                restart = false;
                OthelloGUI g = new OthelloGUI(pair.ai1(), pair.ai2(), size, false);

                f.getContentPane().add(g);
                f.setVisible(true);

                while (!restart) {
                    Thread.sleep(1);
                    g.mouseClicked(null);
                }

                f.getContentPane().remove(g);
            }
        }
        catch (IOException e){
        	errMsg = "Images not found at " + System.getProperty("user.dir") + "\\imgs";
        	err = true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(err) {
        	printHelp(errMsg);
           	System.exit(1);
        }
    }

    /**
     * Printing error and help-message
     */
    public static void printHelp(String errMsg) {
        if(!errMsg.equals("")) {
            System.err.println(errMsg);
        }
        System.err.println("Usage: java PlayOthello OthelloAI1 OthelloAI2 [size]");
        System.err.println("\tOthelloAI1\t - Either '" + HUMAN_CMD +"' indicating a human will be playing, or specify an OthelloAI class implementing IOthelloAI.");
        System.err.println("\tOthelloAI2\t - Must always specify an OthelloAI class implementing IOthelloAI.");
        System.err.println("\tsize\t\t - Must be an even integer greater or equal to 4. Defaults to 8.");
    }

    /**
     * Returns an instance of the specified class implementing IOthelloLogic
     * @param cmdParam String from the command line that should be a path to a java class implementing IOthelloLogic
     * @throws TBD
     */
    public static IOthelloAI parseGameLogicParam(String cmdParam) 
            throws ClassNotFoundException, NoSuchMethodException, 
                   InstantiationException, IllegalAccessException,
                   InvocationTargetException {    	
    	IOthelloAI retGL = null;
    	if(!cmdParam.equalsIgnoreCase(HUMAN_CMD)) 
            retGL = (IOthelloAI)Class.forName(cmdParam).getConstructor().newInstance();
    	return retGL;
    }
}
