import bonzai.api.*;

import java.util.*;

public class CompetitorAI implements AI {
	private WeightComparator wizWeight = new CompetitorWeightComparator(0);
	private WeightComparator blkWeight = new CompetitorWeightComparator(1);
	private WeightComparator clnWeight = new CompetitorWeightComparator(2);
	private WeightComparator trcWeight = new CompetitorWeightComparator(3);
	private WeightComparator hatWeight = new CompetitorWeightComparator(4);
	ArrayList<Hat> Hats;
	ArrayList<Hat> RogueHats;
	ArrayList<Wizard> EnemWiz;

	Node goal;

	ArrayList<Hat> NeutHats;
	int RandCount = 0;
	int RandWizX = 0;
	int RandWizY = 0;
	Node RandNode;

	HashMap<Integer, int[]> scoutGoals = new HashMap<Integer, int[]>();
	HashMap<Integer, Integer> scoutHats = new HashMap<Integer, Integer>();

	
	/**
	 * You must have this function, all of the other functions in 
	 * this class are optional.
	 */
	@Override
	public void takeTurn(AIGameState state) {
		this.moveScouts(state);
		this.moveWizard(state);
		this.moveBlockers(state);
		this.moveCleaners(state);
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
		NeutHats = state.getNeutralHats();
		int BaseHats = 0;
		ArrayList<Node> lPath;
		int[] teams = new int[numPlayers - 1];
		int index = 0;
		int pLength = 10000;
		int bail = 0;
		
		if( RandCount % 10 == 0  || wizard.getLocation().equals(state.getNode( RandWizX, RandWizY ))) {
			RandWizX = (int)((Math.random() * 1000 ) % state.getWidth());
			wizard.shout( "X" + RandWizX );
			RandWizY = (int)((Math.random() * 1000 ) % state.getHeight());
			wizard.shout( "Y:" + RandWizY );

		}
		
		RandCount++;

		
		for (int i = 1; i <= numPlayers; i++) {
			if (i != teamNumber) {
				teams[index] = i;
				index++;
			}
		}
		
		
		for( Hat lHat : Hats ) {
			if( lHat.getLocation().isBase() ) BaseHats++;
		}
		
		goal = state.getBase(teams[ (int)((Math.random() * 100) % (numPlayers -1) ) ]);
		
		if( !RogueHats.isEmpty() ) {
			wizard.shout( "rogues" );
			for( Hat lHat : RogueHats ) {
				lPath = state.getPath(wizard, lHat.getLocation(), wizWeight );
				if( lPath.size() < pLength ) {
					pLength = lPath.size();
					goal = lHat.getLocation();
				}
				bail++;
				if( bail > 1 ) {
					bail = 0;
					break;
				}
			}
			pLength = 10000;
		} else if( !NeutHats.isEmpty() ) {
			wizard.shout( "neutrals");
			for( Hat lHat : NeutHats ) {
				lPath = state.getPath(wizard, lHat.getLocation(), wizWeight );
				if( lPath.size() < pLength ) {
					pLength = lPath.size();
					goal = lHat.getLocation();
				}
				bail++;
				if( bail > 1 ) {
					bail = 0;
					break;
				}
			}
			pLength = 10000;
		} else if(  BaseHats <= Hats.size() && BaseHats < 2 ) {
			wizard.shout( "BASE" );
			goal = state.getBase(teams[ (int)((Math.random() * 100) % (numPlayers -1) ) ]);
		} else if( !NeutHats.isEmpty() ) {
			for( Hat lHat : NeutHats ) {
				lPath = state.getPath(wizard, lHat.getLocation(), wizWeight );
				if( lPath.size() < pLength ) {
					pLength = lPath.size();
					goal = lHat.getLocation();
				}
				bail++;
				if( bail > 1 ) {
					bail = 0;
					break;
				}
			}
			pLength = 10000;
		} else if( !EnemWiz.isEmpty() ) {
			wizard.shout( "ENEMY WIZARD" );
			for( Wizard lWiz : EnemWiz ) {
				lPath = state.getPath(wizard, lWiz.getLocation(), wizWeight );
				if( lPath.size() < pLength ) {
					pLength = lPath.size();
					goal = lWiz.getLocation();
				}
				
				if( pLength > 2 || lWiz.getLocation().equals( wizard.getLocation() )) {
					goal = state.getNode( RandWizX, RandWizY );
				}
				
				bail++;
				if( bail > 1 ) {
					bail = 0;
					break;
				}
			}
			pLength = 10000;
		} else {
			wizard.shout( "RANDOM");
			goal = state.getNode( RandWizX, RandWizY );
		}
	
		//Wizard Pathfinding
		moveActor(wizard, goal, wizWeight );
		
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
			for( Hat mHat : Hats ) {
				if (Hats.size() < 2 || blocker.isAdjacent(mHat) || 
						blocker.isAdjacent(state.getMyWizard()) || 
						blocker.getLocation() == state.getMyWizard().getLocation()) {
					blocker.unBlock();
				} else if (blocker.getLocation().getActors().contains(EnemWiz)) {
					blocker.block();
				} else {
					blocker.block();
				}
			}
			if( !EnemWiz.isEmpty() ) {
				for( Wizard lWiz : EnemWiz ) {
					moveActor(blocker, lWiz.getLocation(), blkWeight );
				}
			} else {
				goal = state.getMyBase();
				if(Math.random() < .9) {
					blocker.move((int)(Math.random()*4));
				} else {
					moveActor(blocker, state.getMyBase(), blkWeight);
				}
			}
		}
	}
	
	/**
	 * Move or sweep with your cleaners.
	 * @param state
	 */
	private void moveCleaners(AIGameState state) {
		for(Cleaner cleaner : state.getMyCleaners()) {
			int moveDirection = cleaner.getDirection(state.getNode(2, 2), clnWeight);
			
			//Move your cleaner one step closer to the node (1, 1)
			if(!cleaner.move(moveDirection)) {
				cleaner.shout("I am unable to move in that direction!");
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
			boolean needGoal = true;
			int[] goalList;
			if(!scoutGoals.containsKey(scout.getID())){
				needGoal = true;
				int [] gli = {0, 0, 0, 0};
				scoutGoals.put(scout.getID(), gli);
				scoutHats.put(scout.getID(), 0);
				goalList = gli;
			}
			else{
				goalList = scoutGoals.get(scout.getID());
				if (goalList[3] > 10000 && (goalList[0] == scout.getLocation().getX() && goalList[1] == scout.getLocation().getY())){
					needGoal = true;
					goalList[3] = 0;
					scoutHats.put(scout.getID(), 0);
				}
			}
			if(needGoal){
				if(!state.getRogueHats().isEmpty()){
					for( Hat rhat : state.getRogueHats()){
						if(!scoutHats.containsValue(rhat.getID()) || scoutHats.get(scout.getID()) == rhat.getID()){
							goalList[2] = -1;
							scoutGoals.put(scout.getID(), goalList);
							scoutHats.put(scout.getID(), rhat.getID());
							goalList[1] = rhat.getLocation().getY();
							goalList[0] = rhat.getLocation().getX();
							needGoal = false;
							break;
						}
					}
				}
				else if(!state.getNeutralHats().isEmpty()){
					for( Hat nhat : state.getNeutralHats()){
						if(!scoutHats.containsValue(nhat.getID()) || scoutHats.get(scout.getID()) == nhat.getID()){
							goalList[2] = -1;
							scoutGoals.put(scout.getID(), goalList);
							scoutHats.put(scout.getID(), nhat.getID());
							goalList[1] = nhat.getLocation().getY();
							goalList[0] = nhat.getLocation().getX();
							needGoal = false;
							break;
						}
					}
				}
				else if(!state.getEnemyHats().isEmpty()){
					for( Hat ehat : state.getEnemyHats()){
						if(!scoutHats.containsValue(ehat.getID()) || scoutHats.get(scout.getID()) == ehat.getID()){
							goalList[2] = -1;
							scoutGoals.put(scout.getID(), goalList);
							scoutHats.put(scout.getID(), ehat.getID());
							goalList[1] = ehat.getLocation().getY();
							goalList[0] = ehat.getLocation().getX();
							needGoal = false;
							break;
						}
					}
				}
				else{
					goalList[2] = 1;
					goalList[1] = ((int)(Math.random()*1000))%state.getHeight();
					goalList[0] = ((int)(Math.random()*1000))%state.getWidth();
					scoutGoals.put(scout.getID(), goalList);
					scoutHats.put(scout.getID(), 0);
				}
			}
			
			Node d = state.getNode(goalList[0], goalList[1]);
			moveActor(scout, d, trcWeight);
			moveActor(scout, d, trcWeight);
			scout.shout("Going to: "+goalList[0]+", "+goalList[1]);
			goalList[3]++;
		}
	}
	
	/**
	 * Do something with your hats!!!
	 * @param state
	 */
	private void moveHats(AIGameState state) {
		for(Hat hat : state.getMyHats()) {
			moveActor(hat, state.getMyBase(), hatWeight);
		}
	}
	
	private void moveActor(Actor a, Node goal, WeightComparator comp) {
		
		int moveDirection = a.getDirection( goal, comp);
		if(moveDirection != -1) { 
			if(a.canMove(moveDirection)) {
				a.move(moveDirection);
			}
		}
	}
}