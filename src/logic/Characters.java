package logic;

public class Characters {

	int level;
	int exp;
	int resist;
	int health;
	int intellect;
	int strength;
	int agility;
	int damage;
	int armor;
	int critChance;
	int critStrike;
	int x;
	int y;
	
	void move( String direction )
	{
		if ( direction.equals("forward") )
		{
			x += 2;
		}
		else if( direction.equals("back"))
		{
			x -= 2;
		}
		else if(direction.equals("jump"))
		{
			y -= 80;
		}
	}
	double meleeAttack()
	{
		double result = strength * 2 + damage + agility * 0.5;
		return result;
	}
	double mageAttack()
	{
		double result = intellect * 2 + damage + agility * 0.5;
		return result;
	}
	double rangeAttack()
	{
		double result = agility * 2 + damage;
		return result;
	}
}
