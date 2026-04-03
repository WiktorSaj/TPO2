/**
 * @author Sajnóg Wiktor s32977
 */

package zad1;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.ZoneId;
import java.util.stream.Collectors;

public class IpWhoIsGeoLookup implements GeoLookup {
	@Override
	public GeoInfo lookup(String ip) throws GeoLookupException {

		String link = "https://ipwho.is/" + ip;
		String res;


		try (
				InputStream inputStream = URI.create(link).toURL().openStream();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
		) {
			res = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (Exception e) {
			throw new GeoLookupException("GeoLookUpException");
		}


		return parseGeoInfo(res);
	}

	public GeoInfo parseGeoInfo(String json) throws GeoLookupException {

		Response response = new Gson().fromJson(json, Response.class);

		if (response == null || !response.success || response.country_code() == null || response.country_code().isBlank() ||
				response.timezone == null ||
				response.timezone.id == null || response.timezone.id.isBlank())
			throw new GeoLookupException("GeoLookupException");

		ZoneId zoneId = ZoneId.of(response.timezone.id);
		return new GeoInfo(response.country_code(), zoneId);
	}


	public record Response(
			boolean success,
			String country_code,
			Timezone timezone
	) {
	}

	public record Timezone(String id) {
	}

}
