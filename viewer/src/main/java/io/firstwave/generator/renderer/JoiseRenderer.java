package io.firstwave.generator.renderer;

import com.sudoplay.joise.module.ModuleAutoCorrect;
import com.sudoplay.joise.module.ModuleBasisFunction;
import com.sudoplay.joise.module.ModuleScaleDomain;
import io.firstwave.generator.noise.Interpolator;
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

		ModuleAutoCorrect correct = new ModuleAutoCorrect();
		correct.setSource(basis);
		correct.calculate();

		ModuleScaleDomain scaleDomain = new ModuleScaleDomain();
		scaleDomain.setSource(correct);
		scaleDomain.setScaleX(4.0);
		scaleDomain.setScaleY(4.0);

		Layer layer = new Layer("Noise", 1000, 1000);
		double [][] v = new double[100][100];
		for (int y = 0; y < 100; y++) {
			for (int x = 0; x < 100; x++) {
				v[x][y] = scaleDomain.get(x, y);

			}
		}

		double[][] iv = new double[1000][1000];
		for (int y = 0; y < 1000; y++) {
			for (int x = 0; x < 1000; x++) {
				float i = (float) Interpolator.CUBIC.get(v, (float) x * 0.1f, (float) y * 0.1f);
				layer.setRGB(x, y, color(1.0f, i, i, i));
			}
		}

		List<Layer> rv = new ArrayList<Layer>();
		rv.add(layer);
		return rv;
	}
}
