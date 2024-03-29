package com.algodal.game.rockpaperscissors.scenes;

import static com.algodal.game.rockpaperscissors.Constants.gplScene;

import com.algodal.game.rockpaperscissors.Scene;
import com.algodal.game.rockpaperscissors.SubGame;
import com.algodal.game.rockpaperscissors.entities.Background;
import com.algodal.game.rockpaperscissors.entities.Buttons;
import com.algodal.game.rockpaperscissors.entities.HandManager;
import com.algodal.game.rockpaperscissors.entities.Labels;
import com.algodal.game.rockpaperscissors.entities.Title;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Play extends Scene {

	//Allow easy communication between objects.
	public final HandManager handManager = new HandManager();
	public final Buttons buttons = new Buttons();
	
	public Play() {
		entities.add(new Background());
		entities.add(handManager);
		entities.add(buttons);
		entities.add(new Labels());
		entities.add(new Title());
	}
	
	@Override
	public void show(SubGame sg) {
		sg.st.clear();
		super.show(sg);
	}
	
	@Override
	public String name() {
		return gplScene;
	}
	
	@Override
	public void backendRender(SubGame sg, float delta) {
		if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			sg.data.play.setting.completedRounds = 0;
			sg.data.play.player01.scoreAmount = 0;
			sg.data.play.player02.scoreAmount = 0;
			((HandManager)sg.getEntity("Hand Manager")).reset();
		}
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			sg.data.play.setting.maxRounds ++;
			if(sg.data.play.setting.maxRounds > 99) sg.data.play.setting.maxRounds = 99;
		}
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			sg.data.play.setting.maxRounds --;
			if(sg.data.play.setting.maxRounds < 3) sg.data.play.setting.maxRounds = 3;
		}
		
		/**   //Don't need to do this because we now have the EndGameDialog.
		if(sg.data.play.setting.completedRounds >= sg.data.play.setting.maxRounds) {
			for(Actor actor : sg.st.getActors()) if(actor.getTouchable() != Touchable.disabled) {
				actor.setTouchable(Touchable.disabled);
			}
			final Buttons b = sg.getEntity("Buttons");
			b.getResetBtn().setTouchable(Touchable.enabled);
		}
		
		if(sg.data.play.setting.completedRounds < sg.data.play.setting.maxRounds) {
			for(Actor actor : sg.st.getActors()) if(actor.getTouchable() != Touchable.enabled) {
				actor.setTouchable(Touchable.enabled);
			}
		}*/
	}
}
