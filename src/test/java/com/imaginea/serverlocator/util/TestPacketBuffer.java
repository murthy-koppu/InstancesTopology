package com.imaginea.serverlocator.util;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class TestPacketBuffer {

	@Test
	public void testValidateInt() {
		Assert.assertEquals(PacketBuffer.isInvalidInt(new byte[]{57,48}), true);
		Assert.assertEquals(PacketBuffer.isInvalidInt(new byte[]{52}),true);
		Assert.assertFalse(PacketBuffer.isInvalidInt(new byte[]{58,48}));
		Assert.assertFalse(PacketBuffer.isInvalidInt(new byte[]{56,99}));
		Assert.assertFalse(PacketBuffer.isInvalidInt(new byte[]{52,(byte)'.'}));		
	}

}
