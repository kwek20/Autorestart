/**
 * 
 */
package de.inventivegames.util.title;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ReflectionUtils {

	/**
	 * Get the craftbukkit class from name
	 * @param ClassName the name
	 * @return the class, or <code>null</code>
	 */
    public static Class<?> getCraftClass(String ClassName) {
        String className = "net.minecraft.server." + getVersion() + ClassName;
        Class<?> c = null;
        try {
            c = Class.forName(className);
        }
        catch (Exception e) { e.printStackTrace(); }
        return c;
    }
    
    /**
	 * @return
	 */
	public static String getVersion() {
		String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1) + ".";
	}

    /**
     * Get the NMS handle from this entity
     * @param entity the entity to get the handle from
     * @return the handle, or <code>null</code>
     */
    public static Object getHandle(Entity entity) {
        try {
            return getMethod(entity.getClass(), "getHandle").invoke(entity);
        }
        catch (Exception e){
            e.printStackTrace(); 
            return null;
        }
    }

    /**
     * Get the NMS world handle
     * @param world the world to get the handle from
     * @return the handle, or <code>null</code>
     */
    public static Object getHandle(World world) {
        try {
            return getMethod(world.getClass(), "getHandle").invoke(world);
        }
        catch (Exception e){
            e.printStackTrace(); 
            return null;
        }
    }

    /**
     * Get the field from a class
     * @param cl the class where were reading from
     * @param field_name the name of the field
     * @return the field, or <code>null</code>
     */
    public static Field getField(Class<?> cl, String field_name) {
        try {
        	cl.getField(field_name).setAccessible(true);
            return cl.getField(field_name);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Run a method :P
     * @param cl the object were running for
     * @param m the method we run
     * @param args the arguments we have for the method
     * @return the return of the method, can be <code>null</code>
     */
    public static Object runMethod(Object cl, Method m, Object... args){
    	try {
			return m.invoke(cl, args);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
    }

    /**
     * Get a method by name from a class {@link WitherListener} optional parameters
     * @param cl the class were reading from
     * @param method the method name
     * @param args the parameters of the method (can be empty)
     * @return the method or <code>null</code>
     */
    public static Method getMethod(Class<?> cl, String method, Class<?>... args) {
        for (Method m : cl.getMethods()) 
            if (m.getName().equals(method) && ClassListEqual(args, m.getParameterTypes()))
                return m;
        return null;
    }

    /**
     * Get a method by name from a class without parameters
     * @param cl the class were reading from
     * @param method the method name
     * @returnthe method or <code>null</code>
     */
    public static Method getMethod(Class<?> cl, String method) {
        for (Method m : cl.getMethods()) {
        	if (m.getName().equalsIgnoreCase(method))
                return m;
        }
        return null;
    }

    /**
     * Check if the first array of classes are of the same type as the second
     * @param l1 the first classes array
     * @param l2 the other array :D
     * @return <code>true</code> if they are, else <code>false</code>
     */
    public static boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
        boolean equal = true;
        if (l1.length != l2.length)
            return false;
        for (int i = 0; i < l1.length; i++)
            if (l1[i] != l2[i]) {
                equal = false;
                break;
            }
        return equal;
    }

    /**
     * Quickly respawn a player
	 * @param plugin the plugin (We need to create a 2 tick task)
	 * @param player the player we want to respawn
	 */
	public static void respawnPlayer(JavaPlugin plugin, final Player player) {
		new BukkitRunnable(){
            public void run(){
                try {
                    Object nmsPlayer = getHandle(player);
                    Object packet = getCraftClass("PacketPlayInClientCommand").newInstance();
                    Field a = packet.getClass().getDeclaredField("a");
                    a.setAccessible(true);
                    
                    a.set(packet, getCraftClass("EnumClientCommand").getEnumConstants()[0]);
                    
                    Object con = getField(nmsPlayer.getClass(), "playerConnection").get(nmsPlayer);
                    con.getClass().getMethod("a", packet.getClass()).invoke(con, packet);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(plugin, 2L);
	}
	
	/**
	 *TODO! Does not work yet
	 * @param item the item to add glow on
	 * @return the new item
	 */
	public static ItemStack addGlow(ItemStack item){
		/*
		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = null;
        if (!nmsStack.hasTag()) {
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        }
        if (tag == null) tag = nmsStack.getTag();
        NBTTagList ench = new NBTTagList();
        tag.set("ench", ench);
        nmsStack.setTag(tag);
        return CraftItemStack.asCraftMirror(nmsStack);
		 */
		
		return item;
	}
}