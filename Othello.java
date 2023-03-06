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
    public static String HUMAN_CMD = "human";
	
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
        
        try{
        	OthelloGUI g = new OthelloGUI(ai1, ai2, size, ai1 == null);

        	// Setup of the frame containing the game
        	JFrame f = new JFrame();
        	f.setSize((size+2)*100,(size+2)*100);
        	f.setTitle("Othello");
        	f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        	f.getContentPane().add(g);    
        	f.setVisible(true);
        }
        catch (IOException e){
        	errMsg = "Images not found at " + System.getProperty("user.dir") + "\\imgs";
        	err = true;
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
