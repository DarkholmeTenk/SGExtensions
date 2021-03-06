package sgextensions;

import ic2.api.recipe.*;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = "SGExtensions", name = "SG Darkcraft Edition", version = "pre2")

@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class SGExtensions
{
	@SidedProxy(clientSide = "sgextensions.ClientProxy", serverSide = "sgextensions.CommonProxy")
	public static CommonProxy proxy;
    @Mod.Instance
    public static SGExtensions instance = new SGExtensions();
	public static GuiHandler guiHandler = new GuiHandler();

	public static SGChannel channel;
	public static BaseTEChunkManager chunkManager;

	public static Block sgBaseBlock;
	public static Block sgRingBlock;
	public static Block sgControllerBlock;
	public static Block sgPortalBlock;
	public static Block naquadahBlock, naquadahOre;

	public static Item naquadah, hardNaquadah, naquadahIngot, sgCoreCrystal, sgControllerCrystal;
	public static Item sgDarkUpgrades, sgHardFuel;
	
	public static int safeFuelMod = 4;
	public static int quickFuelMod = 10;

	public static boolean addOresToExistingWorlds;
	public static boolean addOres;
	public static boolean irisKillClearInv;
	
	public static int irisFrames = 20;
	
	public static int bcPowerPerFuel;
	public static int icPowerPerFuel;
	public static int fuelAmount;
	public static int fuelStore = 50;
	public static int maxOpenTime;
	
	public static boolean fuelHardMode;
	public static boolean recieveHardMode;
	public static boolean recieveKill;
	public static boolean explosionsEnabled = true;
	public static ItemStack stargateFuel;

	public static NaquadahOreWorldGen naquadahOreGenerator;
	public static Block diallerBlock;
	public static Block sgDarkPowerBlock;
	public static Block sgDarkEngineBlock;
	public static final int GUIELEMENT_GATE = 1;
	public static final int GUIELEMENT_DHD = 2;
	public static final int GUIELEMENT_GDO = 3;
	public static SGDarkAddressStore AddressStore;
	
	public static final String baseLocation = "sgextensions:";
	
	public static SGDarkGDO sgDarkGDOItem;

	@Mod.PreInit()
	public void preInit(FMLPreInitializationEvent e)
	{
		ConfigHandler.loadConfig(e);
	}

	@Mod.Init
	public void load(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(this);
		proxy.registerRenderThings();
		channel = new SGChannel(Info.modID);
		chunkManager = new BaseTEChunkManager(this);
		addOresToExistingWorlds = ConfigHandler.regenOres;
		addOres = ConfigHandler.addOres;
		bcPowerPerFuel = ConfigHandler.bcPower;
		icPowerPerFuel = ConfigHandler.icPower;
		fuelAmount = ConfigHandler.fuelAm;
		maxOpenTime = ConfigHandler.maxOpen;
		irisKillClearInv = ConfigHandler.irisKillClear;
		fuelHardMode = ConfigHandler.fuelHardMode;
		recieveHardMode = ConfigHandler.recieveHardMode;
		recieveKill = ConfigHandler.recieveKill;
		
		registerItems();
        registerBlocks();
        registerDarkRecipes();
        registerRandomItems();
        registerTradeHandlers();
        registerWorldGenerators();
		proxy.ProxyInit();
		NetworkRegistry.instance().registerGuiHandler(this,new GuiHandler());
		LanguageRegistry.instance().addStringLocalization("itemGroup.tabStargate", "Stargate");
		AddressStore = new SGDarkAddressStore();
	}
	
	@ServerStarting
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new SGDarkECommand());
	}
	
	SGDarkMultiItem registerMI(int ID,String[] sN, String[] icons,String[] info,int StackSize)
	{
		SGDarkMultiItem Temp = new SGDarkMultiItem(ID,sN,icons,info,StackSize);
		return Temp;
	}
	
	void registerDarkRecipes()
	{
		ShapedOreRecipe TempRec;
		ItemStack darkUpgradeIris = new ItemStack(sgDarkUpgrades,1,2);
		ItemStack darkUpgradeFast = new ItemStack(sgDarkUpgrades,1,0);
		ItemStack darkUpgradeSafe = new ItemStack(sgDarkUpgrades,1,1);
		ItemStack darkHardUnstable = new ItemStack(sgHardFuel,1,0);
		ItemStack darkHardStable = new ItemStack(sgHardFuel,1,1);
		ItemStack darkHardDust = new ItemStack(sgHardFuel,1,2);
		ItemStack darkHardStableDust = new ItemStack(sgHardFuel,1,3);
		
		
		TempRec = new ShapedOreRecipe(darkUpgradeIris,true,new Object[]{
			"III","IDI","III",
			Character.valueOf('I'),"ingotIron",Character.valueOf('D'),sgControllerCrystal});
		GameRegistry.addRecipe(TempRec);
		
		TempRec = new ShapedOreRecipe(darkUpgradeFast,true,new Object[]{
				"DID","IDI","DID",
				Character.valueOf('I'),darkHardStable,Character.valueOf('D'),sgCoreCrystal});
		GameRegistry.addRecipe(TempRec);
		
		TempRec = new ShapedOreRecipe(darkUpgradeSafe,true,new Object[]{
				"XIX","TDT","XIX",
				Character.valueOf('X'),darkHardStable,Character.valueOf('I'),darkUpgradeFast,
				Character.valueOf('T'),darkUpgradeIris,Character.valueOf('D'),sgCoreCrystal});
		GameRegistry.addRecipe(TempRec);
		
		TempRec = new ShapedOreRecipe(darkUpgradeSafe,true,new Object[]{
				"XIX","TDT","XIX",
				Character.valueOf('X'),darkHardStable,Character.valueOf('T'),darkUpgradeFast,
				Character.valueOf('I'),darkUpgradeIris,Character.valueOf('D'),sgCoreCrystal});
		GameRegistry.addRecipe(TempRec);
		
		TempRec = new ShapedOreRecipe(darkHardUnstable,true,new Object[]{
				"DND","NNN","DND",
				Character.valueOf('D'),Item.diamond,Character.valueOf('N'),naquadahIngot});
		GameRegistry.addRecipe(TempRec);
		
		TempRec = new ShapedOreRecipe(darkHardStable,true,new Object[]{
				"DGD","DND","DGD",
				Character.valueOf('D'),Item.diamond,Character.valueOf('N'),darkHardUnstable,Character.valueOf('G'),Block.glass});
		GameRegistry.addRecipe(TempRec);
		
		TempRec = new ShapedOreRecipe(sgDarkGDOItem,true,new Object[]{
				"IGI","IBI","III",
				Character.valueOf('I'),Item.ingotIron,Character.valueOf('G'),Block.thinGlass,Character.valueOf('B'),Block.stoneButton
		});
		GameRegistry.addRecipe(TempRec);
		
		if(!addOres)
		{
			TempRec = new ShapedOreRecipe(naquadah,true,new Object[]{
			"-C-","GCG","-C-",
			Character.valueOf('C'),Item.coal,Character.valueOf('G'),"dyeGreen"});
			GameRegistry.addRecipe(TempRec);
		}
		
		if(Loader.isModLoaded("IC2"))
		{
			ic2.api.recipe.Recipes.macerator.addRecipe(new ItemStack(naquadah,1,0), new ItemStack(sgHardFuel,2,2));
			TempRec = new ShapedOreRecipe(darkHardStableDust,true,new Object[]{
					"DGD","DND","DGD",
					Character.valueOf('D'),"dustDiamond",Character.valueOf('N'),darkHardDust,Character.valueOf('G'),Block.sand});
			GameRegistry.addRecipe(TempRec);
			ic2.api.recipe.Recipes.compressor.addRecipe(darkHardStableDust,darkHardStable);
		}
		
		if(Loader.isModLoaded("ComputerCraft"))
		{
			TempRec = new ShapedOreRecipe(diallerBlock,true,new Object[]{
					"SRS","RCR","SRS",
					Character.valueOf('S'),Block.stone,Character.valueOf('R'),Item.redstone,Character.valueOf('C'),sgControllerCrystal});
			GameRegistry.addRecipe(TempRec);
		}
		
		TempRec = new ShapedOreRecipe(sgDarkPowerBlock,true,new Object[]{
				"GNG","NDN","GNG",
				Character.valueOf('G'),"ingotGold",Character.valueOf('N'),naquadahIngot,Character.valueOf('D'),Item.diamond
		});
	}
	
	void registerMultiItems()
	{
		//UPGRADES
		String[] a = new String[]  {"Stargate Upgrade - Fast Dial", "Stargate Upgrade - Safe Dial", "Stargate Upgrade - Iris","Stargate Upgrade - Admin"};
		String [] b =  new String[] 
		{	"Allows instant dialling#at 10 * energy cost",
			"Allows dialling with no kawoosh#at 4 * energy cost",
			"Allows computer controlled iris",
			"No energy usage, unbreakable, etc"};
		String[] c = new String[]{"upFD","upSD","upIR","upAD"};
		SGDarkMultiItem TempUpgrades = registerMI(ConfigHandler.itemUpgradesID,a,c,b,1);
		GameRegistry.registerItem((Item) TempUpgrades, "sgDarkUpgrades");
		sgDarkUpgrades = TempUpgrades;
		
		a = new String[] {"Unstable Naquadriah","Naquadriah","Naquadah Dust","Naquadriah Dust"};
		b = new String[] {};
		c = new String[]{"naqUn","naqSt","naqDu","naqSDu"};
		SGDarkMultiItem TempHardFuel = registerMI(ConfigHandler.itemHardID,a,c,b,64);
		GameRegistry.registerItem((Item) TempHardFuel, "sgDarkHardFuel");
		sgHardFuel = TempHardFuel;
	}
	void registerBlocks()
	{
		diallerBlock = new SGDarkDiallerBlock(ConfigHandler.blockDiallerID).setUnlocalizedName("diallerblock");
		sgDarkPowerBlock = new SGDarkPowerBlock(ConfigHandler.blockPowererID, 0).setUnlocalizedName("powererblock");
		sgBaseBlock = new SGBaseBlock(ConfigHandler.blockSGBaseID).setUnlocalizedName("stargateBase");
		sgRingBlock = new SGRingBlock(ConfigHandler.blockSGRingID).setUnlocalizedName("stargateRing");
		sgControllerBlock = new SGControllerBlock(ConfigHandler.blockSGControllerID).setUnlocalizedName("stargateController");
		sgPortalBlock = new SGPortalBlock(ConfigHandler.blockSGPortalID).setUnlocalizedName("stargatePortal");
		naquadahBlock = new NaquadahBlock(ConfigHandler.blockNaquadahID).setUnlocalizedName("naquadahBlock");
		naquadahOre = new NaquadahOreBlock(ConfigHandler.blockOreNaquadahID).setUnlocalizedName("naquadahOre");
		sgDarkEngineBlock = new SGDarkEngineBlock(ConfigHandler.blockEngineID).setUnlocalizedName("engineBlock");
		
		ItemStack chiselledSandstone = new ItemStack(Block.sandStone, 1, 1);
		ItemStack smoothSandstone = new ItemStack(Block.sandStone, 1, 2);
		ItemStack sgChevronBlock = new ItemStack(sgRingBlock, 1, 1);

		GameRegistry.registerBlock(diallerBlock, "blockDialler");
		GameRegistry.registerTileEntity(SGDarkDiallerTE.class, "tileDialler");
		GameRegistry.addRecipe(new ItemStack(diallerBlock, 1), "III", "RDR", "III", 'I', Item.ingotIron, 'R', Item.redstone, 'D', sgControllerCrystal);
		LanguageRegistry.addName(diallerBlock, "Dialling Computer");

		GameRegistry.registerBlock(sgDarkPowerBlock, "blockPowerer");
		GameRegistry.registerTileEntity(SGDarkPowerTE.class, "sgDarkPowerTE");
		GameRegistry.addRecipe(new ItemStack(sgDarkPowerBlock, 1), "IRI", "GDG", "IRI", 'I', Item.ingotIron, 'R', Item.redstone, 'G', Item.ingotGold, 'D', Item.diamond);
		LanguageRegistry.addName(sgDarkPowerBlock, "Stargate Power Interface");

		GameRegistry.registerBlock(sgRingBlock, SGRingItem.class,"stargateRing");
		GameRegistry.registerTileEntity(SGRingTE.class,"SGRingTE");
		GameRegistry.addRecipe(new ItemStack(sgRingBlock,1),"CCC", "NNN", "SSS",'S', smoothSandstone, 'N', naquadahIngot, 'C', chiselledSandstone);
        LanguageRegistry.addName(sgRingBlock,"Stargate Ring Block");
		GameRegistry.addRecipe(sgChevronBlock, "CgC", "NpN", "SrS",'S', smoothSandstone, 'N', naquadahIngot, 'C', chiselledSandstone,'g', Item.lightStoneDust, 'r', Item.redstone, 'p', Item.enderPearl);
        LanguageRegistry.addName(sgChevronBlock,"Stargate Chevron Block");

		GameRegistry.registerBlock(sgBaseBlock,"stargateBase");
		GameRegistry.registerTileEntity(SGBaseTE.class,"SGBaseTE");
		GameRegistry.addRecipe(new ItemStack(sgBaseBlock,1),"CrC", "NeN", "ScS",'S', smoothSandstone, 'N', naquadahIngot, 'C', chiselledSandstone,'r', Item.redstone, 'e', Item.eyeOfEnder, 'c', sgCoreCrystal);
        LanguageRegistry.addName(sgBaseBlock,"Stargate Base Block");

		GameRegistry.registerBlock(sgControllerBlock,"stargateController");
		GameRegistry.registerTileEntity(SGControllerTE.class,"SGControllerTE");
		GameRegistry.addRecipe(new ItemStack(sgControllerBlock, 1),"bbb", "OpO", "OcO",'b', Block.stoneButton, 'O', Block.obsidian, 'p', Item.enderPearl,'r', Item.redstone, 'c', sgControllerCrystal);
        LanguageRegistry.addName(sgControllerBlock, "Dial-Home Device");

		GameRegistry.registerBlock(naquadahBlock, "naquadahBlock");
		GameRegistry.addRecipe(new ItemStack(naquadahBlock, 1),"nnn","nnn","nnn",'n',naquadahIngot);
        LanguageRegistry.addName(naquadahBlock, "Naquadah Block");

		GameRegistry.registerBlock(naquadahOre, "naquadahOre");
        LanguageRegistry.addName(naquadahOre, "Naquadah Ore");
        
        GameRegistry.registerBlock(sgDarkEngineBlock, "naquadahEngine");
        LanguageRegistry.addName(sgDarkEngineBlock, "Naquadah Engine");
	}
	void registerItems()
	{
        String blueDye = new String("dyeBlue");
        String orangeDye = new String("dyeOrange");

        naquadah = new BaseItem(ConfigHandler.itemNaquadahID).setUnlocalizedName("naquadah");
        naquadahIngot = new BaseItem(ConfigHandler.itemNaqIngotID).setUnlocalizedName("naquadahIngot");
        sgCoreCrystal = new BaseItem(ConfigHandler.itemCrystalCoreID).setUnlocalizedName("sgCrystalCore");
        sgControllerCrystal = new BaseItem(ConfigHandler.itemCrystalControlID).setUnlocalizedName("sgCrystalControl");
        sgDarkGDOItem = new SGDarkGDO(ConfigHandler.itemGDOID);
		
        GameRegistry.registerItem(sgDarkGDOItem, "toolGDO");
        LanguageRegistry.addName(sgDarkGDOItem, "Stargate GDO");
        
        GameRegistry.registerItem(naquadah,"itemNaquadah");
        LanguageRegistry.addName(naquadah,"Naquadah");

		GameRegistry.registerItem(naquadahIngot, "itemNaqIngot");
        LanguageRegistry.addName(naquadahIngot, "Naquadah Ingot");
        GameRegistry.addShapelessRecipe(new ItemStack(naquadahIngot, 1), new ItemStack(naquadah, 1),new ItemStack(Item.ingotIron, 1));

		GameRegistry.registerItem(sgCoreCrystal, "crystalSGCore");
        LanguageRegistry.addName(sgCoreCrystal, "Stargate Core Crystal");
        GameRegistry.addRecipe(new ShapedOreRecipe(sgCoreCrystal, true, new Object[]{
        		"bbr", "rdb", "brb",
        		Character.valueOf('b'), blueDye, Character.valueOf('r'), Item.redstone, Character.valueOf('d'), Item.diamond
        }));

		GameRegistry.registerItem(sgControllerCrystal, "crystalSGControl");
        LanguageRegistry.addName(sgControllerCrystal, "DHD Control Crystal");
        GameRegistry.addRecipe(new ShapedOreRecipe(sgControllerCrystal, true, new Object[]{
        		"roo", "odr", "oor",
        		Character.valueOf('o'), orangeDye, Character.valueOf('r'), Item.redstone, Character.valueOf('d'), Item.diamond
        }));
        
        registerMultiItems();
        if(fuelHardMode)
        {
        	ItemStack tv = new ItemStack(sgHardFuel);
        	tv.setItemDamage(1);
        	stargateFuel = tv;
        }
        else
        {
        	stargateFuel = new ItemStack(naquadah);
        }
	}
	void registerRandomItems()
	{
		String[] categories = {ChestGenHooks.MINESHAFT_CORRIDOR,
				ChestGenHooks.PYRAMID_DESERT_CHEST, ChestGenHooks.PYRAMID_JUNGLE_CHEST,
				ChestGenHooks.STRONGHOLD_LIBRARY};
		addRandomChestItem(new ItemStack(sgBaseBlock), 1, 1, 1, categories);
		addRandomChestItem(new ItemStack(sgRingBlock, 1, 0), 1, 15, 1, categories);
		addRandomChestItem(new ItemStack(sgRingBlock, 1, 1), 1, 13, 1, categories);
	}
    void addRandomChestItem(ItemStack stack, int minQty, int maxQty, int weight, String... category)
    {
        WeightedRandomChestContent item = new WeightedRandomChestContent(stack, minQty, maxQty, weight);
        for (int i = 0; i < category.length; i++)
            ChestGenHooks.addItem(category[i], item);
    }
	void registerWorldGenerators()
	{
		naquadahOreGenerator = new NaquadahOreWorldGen();
		GameRegistry.registerWorldGenerator(naquadahOreGenerator);
	}

	void registerTradeHandlers()
	{
		VillagerRegistry reg = VillagerRegistry.instance();
		SGTradeHandler handler = new SGTradeHandler();
		for (int i = 0; i < 5; i++)
			reg.registerVillageTradeHandler(i, handler);
	}

	@ForgeSubscribe
	public void onChunkLoad(ChunkDataEvent.Load e)
	{
		Chunk chunk = e.getChunk();
		//System.out.printf("SGCraft.onChunkLoad: (%d, %d)\n", chunk.xPosition, chunk.zPosition);
		SGChunkData.onChunkLoad(e);
	}

	@ForgeSubscribe
	public void onChunkSave(ChunkDataEvent.Save e)
	{
		Chunk chunk = e.getChunk();
		//System.out.printf("SGCraft.onChunkSave: (%d, %d)\n", chunk.xPosition, chunk.zPosition);
		SGChunkData.onChunkSave(e);
	}
	
	public static CreativeTabs sgCreative = new CreativeTabs("tabStargate")
	{
		public ItemStack getIconItemStack()
		{
			return new ItemStack(sgBaseBlock,1,0);
		}
	};
	
}