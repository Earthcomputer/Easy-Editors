package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.util.EnumParticleTypes;

public interface IParticleSelectorCallback {

	EnumParticleTypes getParticle();

	void setParticle(EnumParticleTypes particle);

}
