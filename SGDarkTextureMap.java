package sgextensions;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureStitched;
import net.minecraft.util.Icon;

public class SGDarkTextureMap implements IconRegister
{
	public final int textureType;
    public final String textureName;
    public final String basePath;
    public final String textureExt;
    private final HashMap mapTexturesStiched = new HashMap();
    private BufferedImage missingImage = new BufferedImage(64, 64, 2);
    private TextureStitched missingTextureStiched;
    private Texture atlasTexture;
    private final List listTextureStiched = new ArrayList();
    private final Map textureStichedMap = new HashMap();
	
	public SGDarkTextureMap(int par1, String par2, String par3Str, BufferedImage par4BufferedImage)
    {
        this.textureType = par1;
        this.textureName = par2;
        this.basePath = par3Str;
        this.textureExt = ".png";
        this.missingImage = par4BufferedImage;
    }
	
	@Override
    public Icon registerIcon(String par1Str)
    {
        if (par1Str == null)
        {
            (new RuntimeException("Don\'t register null!")).printStackTrace();
            par1Str = "null"; //Don't allow things to actually register null..
        }

        TextureStitched texturestitched = (TextureStitched)this.textureStichedMap.get(par1Str);

        if (texturestitched == null)
        {
            texturestitched = TextureStitched.makeTextureStitched(par1Str);
            this.textureStichedMap.put(par1Str, texturestitched);
        }

        return texturestitched;
    }

}
