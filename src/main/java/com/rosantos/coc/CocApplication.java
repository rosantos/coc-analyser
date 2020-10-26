package com.rosantos.coc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import com.rosantos.coc.model.Clan;
import com.rosantos.coc.model.War;
import com.rosantos.coc.services.ClanService;
import com.rosantos.coc.services.ExportGoogleSheetService;
import com.rosantos.coc.services.ExportService;

@SpringBootApplication
@PropertySources({
    @PropertySource(value = "${coc.app.properties.file}", ignoreResourceNotFound = false),
    @PropertySource(value = "${coc.app.strings.file}", ignoreResourceNotFound = false)
})
public class CocApplication {

	@Autowired
	ClanService clanService;

	
	@Autowired
	ExportService exportService;

	@Autowired
	ExportGoogleSheetService exportGoogleSheet;

	public static void main(String[] args) {
		SpringApplication.run(CocApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			for (int i = 0; i < args.length; i++) {
				String clanTag = args[i];
				
				//Clan-Push
				Clan clan= clanService.getClan(clanTag);
				War war = clanService.getWar(clan);
				clan = clanService.updateClan(war,clan);
				exportGoogleSheet.savePush(clan);
				exportGoogleSheet.saveClan(clan);
				exportGoogleSheet.saveWar(clan);

				System.out.println("Saved "+args[i]);
//				ClanLeague clanLeague = clanService.getClanLeague(clanTag);
//				List<War> wars = clanService.getWarsLeague(clanTag, clanLeague);
//				exportService.saveWar(clanLeague, wars, clanTag);

			}
			int x = SpringApplication.exit(ctx, new ExitCodeGenerator() {
				
				@Override
				public int getExitCode() {
					// TODO Auto-generated method stub
					return 5;
				}
			});
			
			System.exit(x);

		};
	}

}
