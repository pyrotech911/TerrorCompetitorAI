import java.util.ArrayList;

import bonzai.api.*;

public class CompetitorWeightComparator implements WeightComparator
{
	int actor;
	
	public CompetitorWeightComparator() {
		super();
	}
	
	public CompetitorWeightComparator(int actor) {
		this.actor = actor;
	}
	
	@Override
	public double compare(Node arg0) {
		int nodeTypes[] = {0, 0, 0, 0, 0};
		ArrayList<Actor> nodeActors = arg0.getActors();
		for (Actor a : nodeActors) {
			if (a.getType() == Actor.WIZARD) {
				nodeTypes[0] = nodeTypes[0]++;
			} else if (a.getType() == Actor.BLOCKER) {
				Blocker b = (Blocker) a;
				if (b.isBlocking()) {
					nodeTypes[1] = nodeTypes[1]++;
				}
			} else if (a.getType() == Actor.CLEANER) {
				nodeTypes[2] = nodeTypes[2]++;
			} else if (a.getType() == Actor.SCOUT) {
				nodeTypes[3] = nodeTypes[3]++;
			} else if (a.getType() == Actor.HAT) {
				nodeTypes[4] = nodeTypes[4]++;
			}
		}
		if (actor == 0) { // Wizard
			if (nodeTypes[0] > 0) {
				return 0;
			} else if (nodeTypes[1] > 0) {
				return 3;
			} else if (nodeTypes[2] > 0) {
				return 0;
			} else if (nodeTypes[3] > 0) {
				return 0;
			} else if (nodeTypes[4] > 0) {
				return 0;
			}
		} else if (actor == 1) { // Blocker
			if (nodeTypes[0] > 0) {
				return 0;
			} else if (nodeTypes[1] > 0) {
				return 1;
			} else if (nodeTypes[2] > 0) {
				return 3;
			} else if (nodeTypes[3] > 0) {
				return 0;
			} else if (nodeTypes[4] > 0) {
				return 0;
			}
		} else if (actor == 2) { // Cleaner
			if (nodeTypes[0] > 0) {
				return 0;
			} else if (nodeTypes[1] > 0) {
				return 0;
			} else if (nodeTypes[2] > 0) {
				return 0;
			} else if (nodeTypes[3] > 0) {
				return 0;
			} else if (nodeTypes[4] > 0) {
				return 0;
			}
		} else if (actor == 3) { // Torch
			if (nodeTypes[0] > 0) {
				return 0;
			} else if (nodeTypes[1] > 0) {
				return 0;
			} else if (nodeTypes[2] > 0) {
				return 0;
			} else if (nodeTypes[3] > 0) {
				return 0;
			} else if (nodeTypes[4] > 0) {
				return 0;
			}
		} else if (actor == 4) { // Hat 
			if (nodeTypes[0] > 0) {
				return 100;
			} else if (nodeTypes[1] > 0) {
				return 3;
			} else if (nodeTypes[2] > 0) {
				return 0;
			} else if (nodeTypes[3] > 0) {
				return 0;
			} else if (nodeTypes[4] > 0) {
				return 0;
			}
		} 
		return 0;
	}
}