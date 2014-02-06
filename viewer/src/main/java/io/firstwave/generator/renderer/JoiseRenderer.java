package io.firstwave.generator.renderer;

import com.sudoplay.joise.Joise;
import com.sudoplay.joise.module.ModuleBasisFunction;
import com.sudoplay.joise.module.ModuleScaleDomain;
import io.firstwave.generator.viewer.Layer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by waxwing on 2/5/14.
 */
public class JoiseRenderer extends Renderer {
	@Override
	public List<Layer> render(Properties properties) {

		ModuleBasisFunction basis = new ModuleBasisFunction();
		basis.setType(ModuleBasisFunction.BasisType.SIMPLEX);
		basis.setSeed(42);
		basis.setInterpolation(ModuleBasisFunction.InterpolationType.CUBIC);

		ModuleScaleDomain scaleDomain = new ModuleScaleDomain();
		scaleDomain.setSource(basis);
		scaleDomain.setScaleX(0.003);
		scaleDomain.setScaleY(0.003);

		Layer layer = new Layer("Noise", 1000, 1000);

		Joise j = new Joise(scaleDomain);
		for (int y = 0; y < 1000; y++) {
			for (int x = 0; x < 1000; x++) {
				float i = (float) j.get(x, y);
				i = (i + 1.0f) * 0.5f;
				layer.setRGB(x, y, color(1.0f, i, i, i));
			}
		}

		List<Layer> rv = new ArrayList<Layer>();
		rv.add(layer);
		return rv;
	}
}
