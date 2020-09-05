package com.rosantos.coc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.rosantos.coc.model.ClanLeague;
import com.rosantos.coc.model.War;
import com.rosantos.coc.services.ClanService;
import com.rosantos.coc.services.ExportService;

@SpringBootApplication
public class CocApplication {

	@Autowired
	ClanService clanService;

	@Autowired
	ExportService exportService;

	public static void main(String[] args) {
		SpringApplication.run(CocApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			for (int i = 0; i < args.length; i++) {
				String clanTag = args[i];
				ClanLeague clanLeague = clanService.getClanLeague(clanTag);
				List<War> wars = clanService.getWarsLeague(clanTag, clanLeague);
				exportService.save(clanLeague, wars, clanTag);
			}

		};
	}

}
