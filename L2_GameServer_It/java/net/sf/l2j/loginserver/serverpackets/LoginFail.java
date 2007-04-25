/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.loginserver.serverpackets;

/**
 * Fromat: d
 * d: the failure reason
 */
public final class LoginFail extends L2LoginServerPacket
{
	public static enum LoginFailReason
	{
		REASON_SYSTEM_ERROR			(0x01),
		REASON_PASS_WRONG			(0x02),
		REASON_USER_OR_PASS_WRONG	(0x03),
		REASON_ACCESS_FAILED		(0x04),
		REASON_ACCOUNT_IN_USE		(0x07),
		REASON_ACCOUNT_BANNED		(0x09);
		
		private final int _code;
		
		LoginFailReason(int code)
		{
			_code = code;
		}
		
		public final int getCode()
		{
			return _code;
		}
	}
	/*public static int REASON_ACCOUNT_BANNED = 0x09;
	public static int REASON_ACCOUNT_IN_USE = 0x07;
	public static int REASON_ACCESS_FAILED = 0x04;
	public static int REASON_USER_OR_PASS_WRONG = 0x03;
	public static int REASON_PASS_WRONG = 0x02;
	public static int REASON_SYSTEM_ERROR = 0x01;*/
	
	private LoginFailReason _reason;
	
	
	public LoginFail(LoginFailReason reason) 
	{
		_reason = reason;
	}

	/**
	 * @see com.l2jserver.mmocore.network.SendablePacket#write()
	 */
	@Override
	protected void write()
	{
		writeC(0x01);
		writeD(_reason.getCode());
	}
}