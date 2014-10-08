/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.CubemapLoader.CubemapParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.FacedCubemapData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Wraps a standard OpenGL ES Cubemap. Must be disposed when it is no longer used.
 * @author Xoppa */
public class Cubemap extends GLTexture {
	private static AssetManager assetManager;
	final static Map<Application, Array<Cubemap>> managedCubemaps = new HashMap<Application, Array<Cubemap>>();

	/** Enum to identify each side of a Cubemap */
	public enum CubemapSide {
		/** The positive X and first side of the cubemap */
		PositiveX(0, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X),
		/** The negative X and second side of the cubemap */
		NegativeX(1, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X),
		/** The positive Y and third side of the cubemap */
		PositiveY(2, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y),
		/** The negative Y and fourth side of the cubemap */
		NegativeY(3, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y),
		/** The positive Z and fifth side of the cubemap */
		PositiveZ(4, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z),
		/** The negative Z and sixth side of the cubemap */
		NegativeZ(5, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);

		/** The zero based index of the side in the cubemap */
		public final int index;
		/** The OpenGL target (used for glTexImage2D) of the side. */
		public final int glEnum;

		CubemapSide (int index, int glEnum) {
			this.index = index;
			this.glEnum = glEnum;
		}

		/** @return The OpenGL target (used for glTexImage2D) of the side. */
		public int getGLEnum () {
			return glEnum;
		}
	}

	protected CubemapData data;

	/** Construct an empty Cubemap. Use the load(...) methods to set the texture of each side. Every side of the cubemap must be set
	 * before it can be used. */
	public Cubemap (CubemapData data) {
		super(GL20.GL_TEXTURE_CUBE_MAP);
		this.data = data;
		load(data);
	}

	/** Construct a Cubemap with the specified texture files for the sides, does not generate mipmaps. */
	public Cubemap (FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ,
		FileHandle negativeZ) {
		this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
	}

	/** Construct a Cubemap with the specified texture files for the sides, optionally generating mipmaps. */
	public Cubemap (FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ,
		FileHandle negativeZ, boolean useMipMaps) {
		this(createTextureData(positiveX, useMipMaps), createTextureData(negativeX, useMipMaps), createTextureData(positiveY,
			useMipMaps), createTextureData(negativeY, useMipMaps), createTextureData(positiveZ, useMipMaps), createTextureData(
			negativeZ, useMipMaps));
	}

	/** Construct a Cubemap with the specified {@link Pixmap}s for the sides, does not generate mipmaps. */
	public Cubemap (Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
		this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
	}

	/** Construct a Cubemap with the specified {@link Pixmap}s for the sides, optionally generating mipmaps. */
	public Cubemap (Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ,
		boolean useMipMaps) {
		this(positiveX == null ? null : new PixmapTextureData(positiveX, null, useMipMaps, false), negativeX == null ? null
			: new PixmapTextureData(negativeX, null, useMipMaps, false), positiveY == null ? null : new PixmapTextureData(positiveY,
			null, useMipMaps, false), negativeY == null ? null : new PixmapTextureData(negativeY, null, useMipMaps, false),
			positiveZ == null ? null : new PixmapTextureData(positiveZ, null, useMipMaps, false), negativeZ == null ? null
				: new PixmapTextureData(negativeZ, null, useMipMaps, false));
	}

	/** Construct a Cubemap with {@link Pixmap}s for each side of the specified size. */
	public Cubemap (int width, int height, int depth, Format format) {
		this(new PixmapTextureData(new Pixmap(depth, height, format), null, false, true), new PixmapTextureData(new Pixmap(depth,
			height, format), null, false, true), new PixmapTextureData(new Pixmap(width, depth, format), null, false, true),
			new PixmapTextureData(new Pixmap(width, depth, format), null, false, true), new PixmapTextureData(new Pixmap(width,
				height, format), null, false, true), new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
	}

	/** Construct a Cubemap with the specified {@link TextureData}'s for the sides */
	public Cubemap (TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY,
		TextureData positiveZ, TextureData negativeZ) {
		super(GL20.GL_TEXTURE_CUBE_MAP);
		minFilter = TextureFilter.Nearest;
		magFilter = TextureFilter.Nearest;
		uWrap = TextureWrap.ClampToEdge;
		vWrap = TextureWrap.ClampToEdge;
		data = new FacedCubemapData(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ);
		load(data);
	}

	/** Sets the sides of this cubemap to the specified {@link CubemapData}. */
	public void load (CubemapData data) {
		if (!data.isPrepared()) data.prepare();
		bind();
		unsafeSetFilter(minFilter, magFilter, true);
		unsafeSetWrap(uWrap, vWrap, true);
		data.consumeCubemapData();
		Gdx.gl.glBindTexture(glTarget, 0);
	}

	public CubemapData getTextureData () {
		return data;
	}

	@Override
	public boolean isManaged () {
		return data.isManaged();
	}

	@Override
	protected void reload () {
		if (!isManaged()) throw new GdxRuntimeException("Tried to reload an unmanaged Cubemap");
		glHandle = createGLHandle();
		load(data);
	}

	@Override
	public int getWidth () {
		return data.getWidth();
	}

	@Override
	public int getHeight () {
		return data.getHeight();
	}

	@Override
	public int getDepth () {
		return 0;
	}

	/** Disposes all resources associated with the texture */
	public void dispose () {
		// this is a hack. reason: we have to set the glHandle to 0 for textures that are
		// reloaded through the asset manager as we first remove (and thus dispose) the texture
		// and then reload it. the glHandle is set to 0 in invalidateAllTextures prior to
		// removal from the asset manager.
		if (glHandle == 0) return;
		delete();
		if (data.isManaged()) if (managedCubemaps.get(Gdx.app) != null) managedCubemaps.get(Gdx.app).removeValue(this, true);
	}

	private static void addManagedCubemap (Application app, Cubemap cubemap) {
		Array<Cubemap> managedCubemapArray = managedCubemaps.get(app);
		if (managedCubemapArray == null) managedCubemapArray = new Array<Cubemap>();
		managedCubemapArray.add(cubemap);
		managedCubemaps.put(app, managedCubemapArray);
	}

	/** Clears all managed textures. This is an internal method. Do not use it! */
	public static void clearAllCubemaps (Application app) {
		managedCubemaps.remove(app);
	}

	/** Invalidate all managed textures. This is an internal method. Do not use it! */
	public static void invalidateAllTextures (Application app) {
		Array<Cubemap> managedCubemapArray = managedCubemaps.get(app);
		if (managedCubemapArray == null) return;

		if (assetManager == null) {
			for (int i = 0; i < managedCubemapArray.size; i++) {
				Cubemap cubemap = managedCubemapArray.get(i);
				cubemap.reload();
			}
		} else {
			// first we have to make sure the AssetManager isn't loading anything anymore,
			// otherwise the ref counting trick below wouldn't work (when a texture is
			// currently on the task stack of the manager.)
			assetManager.finishLoading();

			// next we go through each texture and reload either directly or via the
			// asset manager.
			Array<Cubemap> cubemaps = new Array<Cubemap>(managedCubemapArray);
			for (Cubemap cubemap : cubemaps) {
				String fileName = assetManager.getAssetFileName(cubemap);
				if (fileName == null) {
					cubemap.reload();
				} else {
					// get the ref count of the texture, then set it to 0 so we
					// can actually remove it from the assetmanager. Also set the
					// handle to zero, otherwise we might accidentially dispose
					// already reloaded textures.
					final int refCount = assetManager.getReferenceCount(fileName);
					assetManager.setReferenceCount(fileName, 0);
					cubemap.glHandle = 0;

					// create the parameters, passing the reference to the texture as
					// well as a callback that sets the ref count.
					CubemapParameter params = new CubemapParameter();
					params.textureData = cubemap.getTextureData();
					params.minFilter = cubemap.getMinFilter();
					params.magFilter = cubemap.getMagFilter();
					params.wrapU = cubemap.getUWrap();
					params.wrapV = cubemap.getVWrap();
					params.cubemap = cubemap; // special parameter which will ensure that the references stay the same.
					params.loadedCallback = new LoadedCallback() {
						@Override
						public void finishedLoading (AssetManager assetManager, String fileName, Class type) {
							assetManager.setReferenceCount(fileName, refCount);
						}
					};

					// unload the texture, create a new gl handle then reload it.
					assetManager.unload(fileName);
					cubemap.glHandle = GLTexture.createGLHandle();
					assetManager.load(fileName, Cubemap.class, params);
				}
			}
			managedCubemapArray.clear();
			managedCubemapArray.addAll(cubemaps);
		}
	}

	/** Sets the {@link AssetManager}. When the context is lost, cubemaps managed by the asset manager are reloaded by the manager
	 * on a separate thread (provided that a suitable {@link AssetLoader} is registered with the manager). Cubemaps not managed by
	 * the AssetManager are reloaded via the usual means on the rendering thread.
	 * @param manager the asset manager. */
	public static void setAssetManager (AssetManager manager) {
		Cubemap.assetManager = manager;
	}

	public static String getManagedStatus () {
		StringBuilder builder = new StringBuilder();
		builder.append("Managed cubemap/app: { ");
		for (Application app : managedCubemaps.keySet()) {
			builder.append(managedCubemaps.get(app).size);
			builder.append(" ");
		}
		builder.append("}");
		return builder.toString();
	}

	/** @return the number of managed textures currently loaded */
	public static int getNumManagedCubemaps () {
		return managedCubemaps.get(Gdx.app).size;
	}

}
