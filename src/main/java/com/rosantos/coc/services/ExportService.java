package com.rosantos.coc.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rosantos.coc.model.ClanLeague;
import com.rosantos.coc.model.ClanWar;
import com.rosantos.coc.model.ConstantsCOC;
import com.rosantos.coc.model.Hero;
import com.rosantos.coc.model.PlayerWar;
import com.rosantos.coc.model.War;
import com.rosantos.coc.model.WarAttack;

@Service
public class ExportService {

	private String pathToSave;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");

	@Autowired
	public ExportService() {
		this.pathToSave = pathToSave;
	}

	public void saveWar(ClanLeague clanLeague, List<War> wars, String clanTag) {
		if (wars.isEmpty()) {
			return;
		}
		
		exportWars(clanLeague, wars, clanTag);
	}

	private void exportWars(ClanLeague clanLeague, List<War> wars, String clanTag) {
		try {
			String fileName = getNameFile(clanLeague, clanTag);
			FileOutputStream out = new FileOutputStream(new File(fileName));

			HSSFWorkbook workbook = new HSSFWorkbook();

			for (War war : wars) {
				HSSFSheet sheet = workbook.createSheet(war.getState().name() + "-" + sdf.format(war.getStartTime()));
				ClanWar clan = war.getClan().getTag().equals(clanTag) ? war.getClan() : war.getOpponent();
				ClanWar enemy = war.getClan().getTag().equals(clanTag) ? war.getOpponent() : war.getClan();
				int numCol = populateClan(sheet, clan, 0, 0);
				populateClan(sheet, enemy, ++numCol, 0);
			}

			workbook.write(out);
			workbook.close();
			out.close();
			System.out.println("Exportado: " + fileName);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String getNameFile(ClanLeague clanLeague, String clanTag) {
		String name = StringUtils.join(Arrays.asList(pathToSave,
				StringUtils.join(Arrays
						.asList((clanLeague != null ? ("league-" + clanLeague.getSeason()) : "war"), clanTag, ".xls")
						.toArray())),
				"/");
		return StringUtils.replace(name, "//", "/");
	}

	private int populateClan(HSSFSheet sheet, ClanWar clan, int initialCol, int numRow) {
		int numCol = initialCol;
		Row row = getOrCreate(sheet, numRow++);// Clan
		Cell cellTag = row.createCell(numCol++, CellType.STRING);
		cellTag.setCellValue(clan.getTag());

		Cell cellName = row.createCell(numCol++, CellType.STRING);
		cellName.setCellValue(clan.getName());

		int maxCol = initialCol;
		row = getOrCreate(sheet, numRow++);
		maxCol = NumberUtils.max(
				processLine(sheet, initialCol, row, Arrays.asList("Level:", clan.getClanLevel(), "Attacks:",
						clan.getAttacks(), "Stars:", clan.getStars(), "Destruction:", clan.getDestructionPercentage())),
				maxCol);
		row = getOrCreate(sheet, numRow++);
		maxCol = NumberUtils.max(processLine(sheet, initialCol, row,
				Arrays.asList("Name", "XP", "Role", "Map Position", "TH", "TH Weapon", "KING", "QUEEN", "WARDEN",
						"CHAMPION", "Attacks", "Stars 1", "Destruction 1", "Stars 2", "Destruction 2",
						"Opponent Attacks", "Opponent Stars", "Oponent Destruction")),
				maxCol);

		List<PlayerWar> sorted = clan.getMembersWar();
		Collections.sort(sorted);

		Integer position = 0;
		for (PlayerWar player : sorted) {
			position++;
			Integer king = null;
			Integer queen = null;
			Integer warden = null;
			Integer champion = null;
			for (Hero hero : player.getHeroes()) {
				switch (hero.getName()) {
				case ConstantsCOC.HERO_KING:
					king = hero.getLevel();
					break;
				case ConstantsCOC.HERO_QUEEN:
					queen = hero.getLevel();
					break;
				case ConstantsCOC.HERO_WARDEN:
					warden = hero.getLevel();
					break;
				case ConstantsCOC.HERO_CHAMPION:
					champion = hero.getLevel();
					break;
				}
			}

			Integer stars1 = null;
			Double destruction1 = null;

			Integer stars2 = null;
			Double destruction2 = null;

			if (player.getAttacks() != null && !player.getAttacks().isEmpty()) {
				List<WarAttack> attacks = player.getAttacks();
				Collections.sort(attacks);
				WarAttack first = attacks.get(0);
				stars1 = first.getStars();
				destruction1 = first.getDestructionPercentage();
				if (attacks.size() > 1) {
					WarAttack second = attacks.get(1);
					stars2 = second.getStars();
					destruction2 = second.getDestructionPercentage();
				}
			}

			Integer starsOpponent = null;
			Double destructionOpponent = null;

			if (player.getBestOpponentAttack() != null) {
				starsOpponent = player.getBestOpponentAttack().getStars();
				destructionOpponent = player.getBestOpponentAttack().getDestructionPercentage();
			}

			row = getOrCreate(sheet, numRow++);
			maxCol = NumberUtils.max(processLine(sheet, initialCol, row,
					Arrays.asList(player.getName(), player.getExpLevel(), player.getRole(), position,
							player.getTownHallLevel(), player.getTownHallWeaponLevel(), king, queen, warden, champion,
							player.getAttacks() != null ? player.getAttacks().size() : null, stars1, destruction1,
							stars2, destruction2, player.getOpponentAttacks(), starsOpponent, destructionOpponent)),
					maxCol);

		}

		return maxCol;
	}

	private Row getOrCreate(HSSFSheet sheet, int i) {
		Row row = sheet.getRow(i);
		if (row == null) {
			row = sheet.createRow(i);
		}
		return row;
	}

	private int processLine(HSSFSheet sheet, int numCol, Row row, List<Object> values) {
		for (Object value : values) {
			Cell cell = null;
			if (value != null) {
				cell = row.createCell(numCol,
						NumberUtils.isCreatable(value.toString()) ? CellType.NUMERIC : CellType.STRING);
				if (value instanceof Integer) {
					cell.setCellValue(Integer.valueOf(value.toString()));
				} else if (value instanceof Double) {
					cell.setCellValue(Double.valueOf(value.toString()));
				} else {
					cell.setCellValue(value.toString());
				}
			} else {
				cell = row.createCell(numCol, CellType.BLANK);
			}
			numCol++;
		}
		return numCol;
	}

}
