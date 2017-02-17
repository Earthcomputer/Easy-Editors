package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;

/*
 * Now let's get straight to the point: the way particles are summoned in Minecraft is stupid.
 * In fact I'd go further than stupid, I would say it is the most idiotic thing anyone has ever
 * come up with in the history of Minecraft development, and it's up against some pretty stupid
 * things.
 * 
 * Now I will admit that this is partly the fault of MCP, as for all we know the code Mojang
 * have in their private repository could be the most beautiful thing the world has ever seen,
 * with the most orthogonality of all computer programs in existence. But in MCP the same
 * variable has been named dx here, xOffset there, and xSpeed somewhere else, and none of them
 * mean the same thing as each other and, worse, none of them give a real hint as to what the
 * variable actually does.
 * 
 * Anyway, this variable, dx as it's called in the /particle command usage (see the translation
 * in en_us.lang for commands.particle.usage) is not only stupidly named, it's stupidly and
 * most ridiculously used. While in one particle type it may actually mean xSpeed, in another
 * it may translate to something like "0.6 times the inverse sine of 2 times pi times xSpeed
 * minus 0.75, and also the red component of the color of the particle, plus the phase of the
 * moon". In fact, there are 47 different summonable particle types in Minecraft as of 1.11.2,
 * and most of those 47 do subtly, or not so subtly, different things with this variable.
 * 
 * So how on Earth is anyone using the /particle command supposed to know what the f**k they're
 * doing?
 * 
 * Well, luckily, if the user has Easy Editors installed they will be able to spawn all their
 * particles without having to worry about these silly inconsistencies... So it all comes down
 * to ME having to deal with OTHER PEOPLE'S brains and whatever they were thinking and what mood
 * they were in when they implemented that specific particle. There is no clean, magic way that
 * Easy Editors can deal with these different particle factories, so that means a LOT of code to
 * write for me.
 * 
 * If you're brave enough to check out a list I spent two f**king hours compiling of each particle
 * and how it deals with the individual parameters of the /particle command, then check out the 100
 * line long text file at /docs/particles.txt. I wish you luck, brave soul, it would be a miracle
 * if your funeral doesn't happen before you reach the end of that file.
 */
public class SyntaxParticle extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		// TODO Auto-generated method stub
		return null;
	}

}
