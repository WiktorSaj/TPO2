/**
 * @author Sajnóg Wiktor s32977
 */

package zad1;


import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class OptionsLoader {
	public GeoTimeOptions load(String fileName) throws Exception {

		try (InputStream inputStream = new FileInputStream(fileName)) {

			Yaml yaml = new Yaml();
			Map<String, Object> options = yaml.load(inputStream);

			if (options == null) {
				throw new Exception("YAML empty");
			}

			String serverZoneId = (String) options.get("serverZoneId");

			if (serverZoneId == null || serverZoneId.isBlank()) {
				throw new Exception("Server Zone ID not found");
			}

			List<String> logLines = (List<String>) options.get("logLines");
			if (logLines == null) {
				logLines = List.of();
			}

			return new GeoTimeOptions(serverZoneId, logLines);

		}

	}
}
