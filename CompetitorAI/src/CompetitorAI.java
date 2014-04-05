import bonzai.api.*;
import java.util.*;

public class CompetitorAI implements AI {
	private WeightComparator pathWeight = new CompetitorWeightComparator();
	ArrayList<Hat> Hats;
	ArrayList<Hat> RogueHats;
	ArrayList<Wizard> EnemWiz;
	
	/**
	 * You must have this function, all of the other functions in 
	 * this class are optional.
	 */
	@Override
	public void takeTurn(AIGameState state) {
		this.moveWizard(state);
		this.moveBlockers(state);
		this.moveCleaners(state);
		this.moveScouts(state);
		this.moveHats(state);
	}
	
	/**
	 * Move or castMagic with your Wizard
	 * @param state
	 */
	private void moveWizard(AIGameState state) {
		Wizard wizard = state.getMyWizard();
		int teamNumber = state.getMyTeamNumber();
		int numPlayers = state.getNumberOfPlayers();
		Hats = state.getMyHats();
		RogueHats = state.getRogueHats();
		EnemWiz = state.getEnemyWizards();
		int[] teams = new int[numPlayers - 1];
		int index = 0;
		Node goal;
		int pLength = 10000;
		for (int i = 1; i <= numPlayers; i++) {
			if (i != teamNumber) {
				teams[index] = i;
				index++;
			}
		}
		
		goal = wizard.getLocation();
		
		if( !RogueHats.isEmpty() ) {
			for( Hat lHat : RogueHats ) {
				ArrayList<Node> lPath = state.getPath(wizard, lHat.getLocation(), pathWeight );
				if( lPath.size() < pLength ) {
					pLength = lPath.size();
					goal = lHat.getLocation();
				}
				pLength = 10000;
			}
		} else if( Hats.size() < 2 ) {
			goal = state.getBase(teams[ (int)(Math.random() % (numPlayers -1) ) ]);
		} else if( !EnemWiz.isEmpty() ) {
			for( Wizard lWiz : EnemWiz ) {
				ArrayList<Node> lPath = state.getPath(wizard, lWiz.getLocation(), pathWeight );
				if( lPath.size() < pLength ) {
					pLength = lPath.size();
					goal = lWiz.getLocation();
				}
				pLength = 10000;
			}
		} else {
			wizard.move((int)(Math.random()*4));
		}
	
		//Wizard Pathfinding
		moveActor(wizard, goal);
		
		//Iterate through all visible enemy actors
		for(Actor e : state.getEnemyActors()) {
			if(wizard.canCast(e)) {
				wizard.castMagic(e);
				wizard.shout("Casting");
			}
		}

		for(Actor e : state.getNeutralActors()) {
			if(wizard.canCast(e)) {
				wizard.castMagic(e);
				wizard.shout("Casting");
			}
		}
	}
	
	/**
	 * Move, block, or unBlock with your blockers.
	 * @param state
	 */
	private void moveBlockers(AIGameState state) {
		for(Blocker blocker : state.getMyBlockers()) {
			if (Hats.size() < 2) {
				blocker.unBlock();
			} else if (blocker.getLocation().getActors().contains(EnemWiz)) {
				blocker.block();
			} else {
				blocker.block();
			}
			blocker.move((int)(Math.random()*4));
		}
	}
	
	/**
	 * Move or sweep with your cleaners.
	 * @param state
	 */
	private void moveCleaners(AIGameState state) {
		for(Cleaner cleaner : state.getMyCleaners()) {
			int moveDirection = cleaner.getDirection(state.getNode(2, 2), pathWeight);
			
			//Move your cleaner one step closer to the node (1, 1)
			if(!cleaner.move(moveDirection)) {
				cleaner.shout("I amteam unable to move in that direction!");
			} else {
				if(!cleaner.canMove(moveDirection)) {
					//There is a blocking blocker in the direction of 'moveDirection'
				}
			}
			
			//If the sweeper can, it uses it's ability on a blocker instead of moving.
			for(Blocker enemyBlocker : state.getEnemyBlockers()) {
				if(cleaner.isAdjacent(enemyBlocker)) {
					cleaner.sweep(enemyBlocker);
				}
			}
		}
	}
	
	/**
	 * Move with your scouts.
	 * @param state
	 */
	private void moveScouts(AIGameState state) {
		for(Scout scout : state.getMyScouts()) {
			if(Math.random() > .8) {
				scout.doubleMove((int)(Math.random()*4), (int)(Math.random()*4));
			} else {
				scout.doubleMove(state.getPath(scout, state.getMyBase(), pathWeight));
			}
		}
	}
	
	/**
	 * Do something with your hats!!!
	 * @param state
	 */
	private void moveHats(AIGameState state) {
		for(Hat hat : state.getMyHats()) {
			moveActor(hat, state.getMyBase());
		}
	}
	
	private void moveActor(Actor a, Node goal) {
		int moveDirection = a.getDirection( goal, pathWeight);
		if(moveDirection != -1) { 
			a.shout("Trying to move");
			if(a.canMove(moveDirection)) {
				a.move(moveDirection);
				a.shout("Moving");
			}
		}
	}
}