package com.algodal.game.rockpaperscissors;

import static com.algodal.game.rockpaperscissors.Constants.defAtlas;
import static com.algodal.game.rockpaperscissors.Constants.defSkin;
import static com.algodal.game.rockpaperscissors.Constants.menScene;
import static com.algodal.game.rockpaperscissors.Constants.radioDark;
import static com.algodal.game.rockpaperscissors.Constants.radioForest;
import static com.algodal.game.rockpaperscissors.Constants.worldHeight;
import static com.algodal.game.rockpaperscissors.Constants.worldWidth;

import com.algodal.game.rockpaperscissors.dialogs.ForceQuitDialog;
import com.algodal.game.rockpaperscissors.dialogs.IDialog;
import com.algodal.game.rockpaperscissors.dialogs.QuickSettingDialog;
import com.algodal.game.rockpaperscissors.entities.LockBG;
import com.algodal.game.rockpaperscissors.entities.LockHand;
import com.algodal.game.rockpaperscissors.helper.PlayHelper;
import com.algodal.game.rockpaperscissors.helper.Shaders;
import com.algodal.game.rockpaperscissors.scenes.Advance;
import com.algodal.game.rockpaperscissors.scenes.Menu;
import com.algodal.game.rockpaperscissors.scenes.Play;
import com.algodal.game.rockpaperscissors.scenes.SkinSetup;
import com.algodal.game.rockpaperscissors.ui.Gallery;
import com.algodal.game.rockpaperscissors.utils.BackgroundSkin;
import com.algodal.game.rockpaperscissors.utils.HandSkin;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SubGame {

	private final SpriteBatch sb;
	private final Viewport vp;
	private final AssetManager am;
	public final Stage st;
	
	private final Color cc;
	private final Vector2 v;
	
	private Scene scene;
	private final Array<Scene> scenes;
	
	private final ArrayMap<String, AssetDescriptor<?>> adMap;
	private final ArrayMap<String, IDialog> dialogMap;
	
	private final InputMultiplexer im;
	
	public final Data data = new Data();
	
	//Facilitates communication between Entities
	public final LockHand lockHand;
	public final Gallery handGallery;
	public final LockBG lockBG;
	public final Gallery bgGallery;
	
	private final Preferences prefs;
	
	@Deprecated
	public boolean player01Turn = true;
	public final PlayHelper playHelper = new PlayHelper();
	public final Play play = new Play(); //Allow easy communication between objects.
	
	//Shaders
	public final Shaders shaders;
	
	//Audio
	final Sound sndClick, sndTone, sndJingle, sndDoor;
	public final Music musEmerald, musMenu;
	
	public void playClick() {
		sndClick.play(data.menu.masterVolume*data.menu.soundVolume);
	}
	
	public void playTone() {
		sndTone.play(data.menu.masterVolume*data.menu.soundVolume);
	}
	
	public void playJingle() {
		sndJingle.play(data.menu.masterVolume*data.menu.soundVolume);
	}
	
	public void playDoor() {
		sndDoor.play(data.menu.masterVolume*data.menu.soundVolume);
	}
	
	public void playEmerald() {
		musEmerald.setLooping(true);
		musEmerald.setVolume(data.menu.masterVolume*data.menu.musicVolume);
		musEmerald.play();
	}
	
	public void playMenu() {
		musMenu.setLooping(true);
		musMenu.setVolume(data.menu.masterVolume*data.menu.musicVolume);
		musMenu.play();
	}
	
	public void pauseEmerald() {
		musEmerald.pause();
	}
	
	public void pauseMenu() {
		musMenu.pause();
	}
	
	public class MusicHandler {
		public int music = 0;
		
		protected void pause() {
			if(music == 1) {
				pauseMenu();
			}
			
			else {
				pauseEmerald();
			}
		}
		
		protected void play() {
			if(music == 1) {
				playMenu();
			}
			
			else {
				playEmerald();
			}
		}
		
		protected void update(int music) {
			if(this.music != music) {
				pause();
				this.music = music;
				play();
			}
		}
	}
	
	public final MusicHandler musicHandler;
	
	public SubGame() {
		sndClick = Gdx.audio.newSound(Gdx.files.internal(Constants.sndClick));
		sndTone = Gdx.audio.newSound(Gdx.files.internal(Constants.sndTone));
		sndJingle = Gdx.audio.newSound(Gdx.files.internal(Constants.sndJingle));
		sndDoor = Gdx.audio.newSound(Gdx.files.internal(Constants.sndDoor));
		
		musEmerald = Gdx.audio.newMusic(Gdx.files.internal(Constants.musEmerald));
		musMenu = Gdx.audio.newMusic(Gdx.files.internal(Constants.musMenu));
		
		musicHandler = new MusicHandler();
		
		shaders = new Shaders();
		
		prefs = Gdx.app.getPreferences("com.algodal.game.rockpaperscissors");
		dataLoad();
		musicHandler.music = data.music;
		musicHandler.play();
		
		sb = new SpriteBatch();
		vp = new FitViewport(worldWidth, worldHeight);
		am = new AssetManager();
		st = new Stage(new FitViewport(worldWidth, worldHeight));
		st.getViewport().getCamera().position.setZero();
		//st.setDebugAll(true);
		
		cc = new Color(Color.BROWN);
		v = new Vector2();
		
		adMap = new ArrayMap<String, AssetDescriptor<?>>();
		adMap.put(defAtlas, new AssetDescriptor<TextureAtlas>(defAtlas, TextureAtlas.class));
		adMap.put(defSkin, new AssetDescriptor<Skin>(defSkin, Skin.class));
		
		handGallery = new Gallery(data.play.setting.handskins);
		bgGallery = new Gallery(data.play.setting.bgskins);
		lockHand = new LockHand();
		lockBG = new LockBG();
		
		scenes = new Array<Scene>();
		scenes.add(new DefaultScene());
		scenes.add(new SkinSetup());
		scenes.add(play);
		scenes.add(new Advance());
		scenes.add(new Menu());
		scene = scenes.get(scenes.size-1); //make non-null as soon as possible
		
		dialogMap = new ArrayMap<String, IDialog>();
		dialogMap.put("quicksetting", new QuickSettingDialog());
		dialogMap.put("forcequit", new ForceQuitDialog());
		
		im = new InputMultiplexer(st, newIP(), newGD());
		Gdx.input.setInputProcessor(im);
		
		//Call LateInitializations
		for(Scene scene : scenes) scene.lateInitialize(this);
		for(Entry<String, IDialog> dialogEntry : dialogMap) dialogEntry.value.getLateInitialization().initialize(this);
		
		//Set scene
		setScene(menScene);
	}
	
	public boolean hasStartedPlaying() {
		if(data.play.setting.completedRounds > 0) return true;
		switch(data.menu.mode) {
		default: return playHelper.single.location == PlayHelper.Single.Step.Player01_Playing;
		case 1: return playHelper.local.location != PlayHelper.Local.Step.Game_Trans;
		}
	}
	
	public boolean isMatchFinished() {
		return data.play.setting.completedRounds >= data.play.setting.maxRounds;
	}
	
	public void reset() {
		data.play.setting.completedRounds = 0;
		data.play.player01.scoreAmount = 0;
		data.play.player02.scoreAmount = 0;
		play.handManager.reset();
	}
	
	public void resetWithoutIndex() {
		data.play.setting.completedRounds = 0;
		data.play.player01.scoreAmount = 0;
		data.play.player02.scoreAmount = 0;
	}
	
	/** updates the game data to the reference fields**/
	public void dataUpdate() {
		data.updateData(prefs);
	}
	
	/** calls dataUpdate() then writes the game data to the files**/
	public void dataSave() {
		data.saveData(prefs);
	}
	
	/** loads the data from file into the fields **/
	public void dataLoad() {
		data.loadData(prefs);
	}
	
	protected void render() {
		 Gdx.gl.glClearColor( cc.r, cc.g, cc.b, cc.a );
		 Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		 
		 scene.render(this, 1f/60);
		 
		 am.update();
		 musicHandler.update(data.music);
	}
	
	protected void resize(int width, int height) {
		vp.update(width, height);
		st.getViewport().update(width, height);
	}
	
	protected void destroy() {
		sb.dispose();
		am.dispose();
		st.dispose();
	}
	
	protected void pause() {
		dataSave();
	}
	
	public void applySpriteViewport() {
		vp.apply();
	}
	
	public void applyStageViewport() {
		st.getViewport().apply();
	}
	
	public void begin(ShaderProgram sp, Color c) {
		sb.setShader(sp);
		sb.setColor((c==null)?Color.WHITE:c);
		sb.setProjectionMatrix(vp.getCamera().combined);
		sb.begin();
	}
	
	public void end() {
		sb.end();
		sb.setShader(null);
		sb.setColor(Color.WHITE);
	}
	
	public void draw(TextureRegion tr, Unit o) {
		sb.draw(tr, o.left(), o.bottom(), o.hw(), o.hh(), o.width, o.height, 1.0f, 1.0f, o.angle);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		load(name);
		return am.get((AssetDescriptor<T>)adMap.get(name));
	}
	
	public void load(String name) {
		final AssetDescriptor<?> ad = adMap.get(name);
		if(!am.isLoaded(ad)) am.load(ad);
		am.finishLoadingAsset(ad);
	}
	
	public void queue(String name) {
		final AssetDescriptor<?> ad = adMap.get(name);
		if(!am.isLoaded(ad)) am.load(ad);
	}
	
	public boolean done() {
		return am.isFinished();
	}
	
	public boolean done(String...names) {
		for(String name : names) {
			final AssetDescriptor<?> ad = adMap.get(name);
			if(!am.isLoaded(ad)) return false;
		}
		return true;
	}
	
	public void setScene(String name) {
		scene = getScene(name);
		scene.show(this);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Scene> T getScene(String name) {
		for(Scene scene : scenes) if(scene.name().equalsIgnoreCase(name)) return (T) scene;
		Gdx.app.log("get scene", "failed to find scene called " + name);
		return null;
	}
	
	@Deprecated
	@SuppressWarnings("unchecked")
	public <T extends Scene> T  getScene() {
		return (T) scene;
	}
	
	public IDialog getDialog(String name) {
		return dialogMap.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IDialog> T dialog(String name) {
		return (T)getDialog(name);
	}
	
	protected Entity getEntity(String name, Scene scene) {
		for(Entity entity : scene.entities) if(entity.name().equalsIgnoreCase(name)) return entity;
		Gdx.app.log("get entity", "failed to find entity called " + name);
		return new Entity.DefaultEntity();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Entity> T getEntity(String name, String scene) {
		final Scene s = getScene(scene);
		return (T) getEntity(name, s);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Entity> T getEntity(String name) {
		return (T) getEntity(name, scene);
	}
	
	private InputProcessor newIP() {
		return new InputProcessor() {
			
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				v.set(screenX, screenY);
				vp.unproject(v);
				scene.up(v.x, v.y);
				return false;
			}
			
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				v.set(screenX, screenY);
				vp.unproject(v);
				scene.drag(v.x, v.y);
				return false;
			}
			
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				v.set(screenX, screenY);
				vp.unproject(v);
				scene.down(v.x, v.y);
				return false;
			}
			
			@Override
			public boolean scrolled(int amount) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean mouseMoved(int screenX, int screenY) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean keyUp(int keycode) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean keyTyped(char character) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean keyDown(int keycode) {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}
	
	private GestureDetector newGD() {
		return new GestureDetector(new GestureListener() {
			
			@Override
			public boolean zoom(float initialDistance, float distance) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean touchDown(float x, float y, int pointer, int button) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean tap(float x, float y, int count, int button) {
				v.set(x, y);
				vp.unproject(v);
				scene.tap(v.x, v.y, count);
				return false;
			}
			
			@Override
			public void pinchStop() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean panStop(float x, float y, int pointer, int button) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean pan(float x, float y, float deltaX, float deltaY) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean longPress(float x, float y) {
				v.set(x, y);
				vp.unproject(v);
				scene.press(v.x, v.y);
				return false;
			}
			
			@Override
			public boolean fling(float velocityX, float velocityY, int button) {
				v.set(velocityX, velocityY);
				vp.unproject(v);
				scene.fling(v.x, v.y);
				return false;
			}
		});
	}
	
	public static class DefaultScene extends Scene {

		@Override
		public void render(SubGame sg, float delta) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public static class Data {
		
		public final Play play = new Play();
		public final Menu menu = new Menu();
		
		public int music = 0; public final static String mID = "music selection";
		
		private final static String play_setting_maxrnds = "Play Setting Maximum Rounds";
		private final static String play_setting_hskini = "Play Setting Hand Skin Index";
		private final static String play_setting_bskini = "Play Setting Background Skin Index";
		private final static String menu_fullgame = "Menu Full Game Purchased";
		private final static String menu_master = "Menu Master Volume";
		private final static String menu_music = "Menu Music Volume";
		private final static String menu_sound = "Menu Sound Volume";
		private final static String menu_mode = "Menu Game Mode";
		
		public final static String[] music_names = new String[] { 
				radioForest, 
				radioDark 
		};
		
		protected void updateData(Preferences prefs) {
			prefs.putInteger(play_setting_maxrnds, play.setting.maxRounds);
			prefs.putInteger(play_setting_hskini, play.setting.handSkinIndex);
			prefs.putInteger(play_setting_bskini, play.setting.bgSkinIndex);
			prefs.putBoolean(menu_fullgame, menu.fullVersionPurchased);
			prefs.putFloat(menu_master, menu.masterVolume);
			prefs.putFloat(menu_music, menu.musicVolume);
			prefs.putFloat(menu_sound, menu.soundVolume);
			prefs.putInteger(menu_mode, menu.mode);
			prefs.putInteger(mID, music);
		}
		protected void saveData(Preferences prefs) {
			updateData(prefs);
			prefs.flush();
		}
		protected void loadData(Preferences prefs) {
			play.setting.maxRounds = prefs.getInteger(play_setting_maxrnds, 3);
			play.setting.handSkinIndex = prefs.getInteger(play_setting_hskini, 0);
			play.setting.bgSkinIndex = prefs.getInteger(play_setting_bskini, 0);
			menu.fullVersionPurchased = prefs.getBoolean(menu_fullgame, false);
			menu.masterVolume = prefs.getFloat(menu_master, 1f);
			menu.musicVolume = prefs.getFloat(menu_music, 1f);
			menu.soundVolume = prefs.getFloat(menu_sound, 1f);
			menu.mode = prefs.getInteger(menu_mode, 0);
			music = prefs.getInteger(mID);
		}
		
		public static class Play {
			public final Player player01 = new Player();
			public final Player player02 = new Player();
			public final Setting setting = new Setting();
			
			public static class Setting {
				public int maxRounds = 3;
				public int completedRounds = 0;
				public HandSkin[] handskins = new HandSkin[] {
						new HandSkin("rock_1", "paper_1", "scissors_1"),
						new HandSkin("rock_0", "paper_0", "scissors_0"),						
						new HandSkin("rock_2", "paper_2", "scissors_2"),
						new HandSkin("rock_3", "paper_3", "scissors_3"),
						new HandSkin("rock_4", "paper_4", "scissors_4"),
						new HandSkin("rock_5", "paper_5", "scissors_5"),
				};
				public BackgroundSkin[] bgskins = new BackgroundSkin[] {
						new BackgroundSkin("bg_6"),	
						new BackgroundSkin("bg_0"),											
						new BackgroundSkin("bg_3"),
						new BackgroundSkin("bg_2"),						
						new BackgroundSkin("bg_4"),
						new BackgroundSkin("bg_5"),
						new BackgroundSkin("bg_1"),
						new BackgroundSkin("bg_7"),
				};
				public int handSkinIndex = 0, bgSkinIndex = 0;
				
				public HandSkin handSkin() {
					return handskins[handSkinIndex];
				}
				
				public BackgroundSkin bgSkin() {
					return bgskins[bgSkinIndex];
				}
			}
			
			public static class Player {
				public int scoreAmount = 0;
			}
		}
		
		public static class Menu {
			public boolean fullVersionPurchased = false;
			public float masterVolume = 1.0f;
			public float soundVolume = 1.0f;
			public float musicVolume = 1.0f;
			public boolean newGame = false;
			public int mode = 0;
		}
	}
}










