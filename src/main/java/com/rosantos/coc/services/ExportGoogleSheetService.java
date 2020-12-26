package com.rosantos.coc.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.chrono.MinguoEra;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.rosantos.coc.model.Clan;
import com.rosantos.coc.model.ClanWar;
import com.rosantos.coc.model.ConstantsCOC;
import com.rosantos.coc.model.EnumOkNok;
import com.rosantos.coc.model.EnumWarState;
import com.rosantos.coc.model.Hero;
import com.rosantos.coc.model.League;
import com.rosantos.coc.model.Player;
import com.rosantos.coc.model.PlayerWar;
import com.rosantos.coc.model.WarAttack;

@Service
public class ExportGoogleSheetService {

	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	@Value("${coc.push.sheetfile}")
	private String pushSheet;

	@Value("${coc.clan.sheetfile}")
	private String clanSheet;

	@Value("${coc.war.sheetfile}")
	private String warSheet;

	@Value("${coc.sheet.members.score.prefix}")
	private String sheetNameScore;

	@Value("${coc.sheet.push.prefix}")
	private String sheetNamePush;

	@Value("${coc.sheet.war.prefix}")
	private String sheetNameWar;

	@Value("#{${coc.scores.trophies}}")
	private Map<String, Double> mapTrophies;

	@Value("#{${coc.th.hitpoints}}")
	private Map<Integer, Double> mapThHitPoint;

	@Value("#{${coc.damage.th12}}")
	private Map<Integer, Object> mapTh12Damage;

	@Value("#{${coc.damage.th13}}")
	private Map<Integer, Object> mapTh13Damage;

	@Value("#{${coc.damage.king}}")
	private Map<Integer, Double> mapKingDamage;

	@Value("#{${coc.damage.queen}}")
	private Map<Integer, Double> mapQueenDamage;

	@Value("#{${coc.damage.warden}}")
	private Map<Integer, Double> mapWardenDamage;

	@Value("#{${coc.damage.champion}}")
	private Map<Integer, Double> mapChampionDamage;

	@Value("#{${coc.th.minimal.leagues}}")
	private Map<Integer, String> mapMinimalLeagues;

	@Value("${coc.scores.attacks.won.value}")
	Double scoreAttacksValue;

	@Value("${coc.scores.donations.quantity}")
	Integer scoreDonationsQuantity;

	@Value("${coc.scores.donations.value}")
	Double scoreDonationsValue;

	@Value("${coc.scores.clan.games.points}")
	Integer scoreClanGamesPoints;

	@Value("${coc.scores.clan.games.value}")
	Double scoreClanGamesValue;

	@Value("${coc.scores.war.attacks.value}")
	Double scoreWarAttacksValue;

	@Value("${coc.scores.war.noattacks.value}")
	Double scoreWarNoAttacksValue;

	@Value("${coc.scores.monthly.goal}")
	Double scoreMonthlyGoal;

	@Autowired
	StringsService strings;

	@Autowired
	LeagueSeasonService leagueSeasonService;

	private Sheets service;

	private List<Object> playerHeaders = new ArrayList<Object>();

	private Map<String, Integer> mapFixedHeaders = new HashMap<String, Integer>();
	private Integer colPlayerTag;
	private Integer colPushPlayerTag;
	private List<Object> playerPush;

	private static SimpleDateFormat sdfDayWar = new SimpleDateFormat("MM-dd");
	private static SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = ExportGoogleSheetService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	@Autowired
	public ExportGoogleSheetService() {
	}

	public void savePush(Clan clan) {
		if (clan == null) {
			return;
		}

		try {
			exportPush(clan);
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	public void saveClan(Clan clan) {
		if (clan == null) {
			return;
		}

		try {
			System.out.println(clanSheet);
			exportClan(clan);
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	private void exportClan(Clan clan) throws GeneralSecurityException, IOException {
		String sheetName = sheetNameScore + "-" + leagueSeasonService.getCurrentSeason() + "-" + clan.getTag();

		ValueRange valueRange = null;
		try {
			valueRange = getService().spreadsheets().values().get(clanSheet, sheetName).execute();
			valueRange = updateClanValues(getService(), clan, sheetName, valueRange);
		} catch (IOException e) {
			valueRange = createClanValues(getService(), sheetName, clan);
		}

		UpdateValuesResponse result = getService().spreadsheets().values()
				.update(clanSheet, valueRange.getRange(), valueRange).setValueInputOption("USER_ENTERED").execute();
		System.out.printf("%d cells updated.", result.getUpdatedCells());
	}

	private ValueRange createClanValues(Sheets service, String sheetName, Clan clan) throws IOException {
		List<Request> requests = new ArrayList<>();
		requests.add(new Request()
				.setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().set("title", sheetName))));

		BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
		service.spreadsheets().batchUpdate(clanSheet, body).execute();

		return updateClanValues(service, clan, sheetName, null);
	}

	private ValueRange updateClanValues(Sheets service, Clan clan, String sheetName, ValueRange valueRange) {
		List<Player> players = clan.getMembers();
		Collections.sort(players, new Comparator<Player>() {

			@Override
			public int compare(Player o1, Player o2) {
				int comp = o2.getTrophies().compareTo(o1.getTrophies());
				return comp == 0 ? o1.getExpLevel().compareTo(o2.getExpLevel()) : comp;
			}
		});

		List<List<Object>> rows = new ArrayList<List<Object>>();
		rows.add(Arrays.asList("=IMAGE(\"" + clan.getShield() + "\")", clan.getName()));
		rows.add(Arrays
				.asList(strings.getMessage(StringsService.UPDATED, sdfTime.format(Calendar.getInstance().getTime()))));
		rows.add(getPlayerHeads(clan, valueRange));

		List<Player> updateds = new ArrayList<Player>();

		List<List<Object>> outs = new ArrayList<List<Object>>();
		if (valueRange != null) {
			List<List<Object>> oldRows = valueRange.getValues();
			boolean foundedHeader = false;
			for (List<Object> oldRow : oldRows) {
				if (!foundedHeader) {
					if (!oldRow.contains(strings.getMessage(StringsService.PLAYER_TAG))) {
						continue;
					}
					foundedHeader = true;
					continue;
				}

				String playerTag = oldRow.get(colPlayerTag) != null ? oldRow.get(colPlayerTag).toString() : null;
				Player player = clan.getPlayer(playerTag);
				if (player == null) {
					outs.add(oldRow);
					continue;
				}

				if (updateds.contains(player)) {
					continue;
				}

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

				Object[] row = new Object[getPlayerHeads(clan, valueRange).size()];

				for (int i = 0; i < row.length; i++) {
					row[i] = StringUtils.EMPTY;
				}

				row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TAG))] = player
						.getTag();
				row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_NAME))] = player
						.getName();
				row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_XP))] = player
						.getExpLevel();
				row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TH))] = player
						.getTownHallLevel();
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_BEST_TROPHIES))] = player.getBestTrophies();
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_LEAGUE))] = getPlayerLeague(player);
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_TROPHIES))] = player.getTrophies();
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_LEAGUE_OK))] = calcPlayerLeagueOk(
								player.getTownHallLevel(), player.getLeague()).name();
				row[getPlayerHeads(clan, valueRange).indexOf(
						strings.getMessage(StringsService.PLAYER_KING))] = king != null ? king : StringUtils.EMPTY;
				row[getPlayerHeads(clan, valueRange).indexOf(
						strings.getMessage(StringsService.PLAYER_QUEEN))] = queen != null ? queen : StringUtils.EMPTY;
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_WARDEN))] = warden != null ? warden
								: StringUtils.EMPTY;
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_CHAMPION))] = champion != null ? champion
								: StringUtils.EMPTY;
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_DONATIONS))] = player.getDonations();
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_ATTACKS))] = player.getAttackWins();

				if (clan instanceof ClanWar) {
					ClanWar clanWar = (ClanWar) clan;
					if (player instanceof PlayerWar) {
						PlayerWar playerWar = (PlayerWar) player;
						List<WarAttack> wins = new ArrayList<WarAttack>();
						if (playerWar.getAttacks() != null) {
							wins = playerWar.getAttacks().stream().filter(attack -> attack.getStars() > 0)
									.collect(Collectors.toList());
							double scoreStars = (wins.stream().map(win -> win.getStars()).reduce(0, Integer::sum)
									* scoreWarAttacksValue)
									+ ((playerWar.getAttacks().size() - ConstantsCOC.WAR_NUMBER_ATTACKS)
											* scoreWarNoAttacksValue);

							row[getPlayerHeads(clan, valueRange)
									.indexOf(getCurrentWarName(clanWar.getWar().getStartTime()))] = scoreStars;

						} else {
							row[getPlayerHeads(clan, valueRange).indexOf(getCurrentWarName(
									clanWar.getWar().getStartTime()))] = (0 - ConstantsCOC.WAR_NUMBER_ATTACKS)
											* scoreWarNoAttacksValue;
						}

					} else {
						row[getPlayerHeads(clan, valueRange)
								.indexOf(getCurrentWarName(clanWar.getWar().getStartTime()))] = StringUtils.EMPTY;
					}
				}

				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_CLAN_GAMES))] = "=MINUS("
								+ player.progressValue(ConstantsCOC.GAMES_PONTUATION) + ";"
								+ getCellName(rows.size() + 1,
										getPlayerHeads(clan, valueRange).indexOf(
												strings.getMessage(StringsService.PLAYER_CLAN_GAMES_INIT)),
										false)
								+ ")";

				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.SCORE_ATTACK))] = "=MULTIPLY("
								+ String.join(";",
										getCellName(rows.size() + 1,
												getPlayerHeads(clan, valueRange).indexOf(
														strings.getMessage(StringsService.PLAYER_ATTACKS)),
												false),
										numberFormat.format(scoreAttacksValue))
								+ ")";

				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.SCORE_DONATIONS))] = "=MULTIPLY(INT(DIVIDE("
								+ getCellName(rows.size() + 1,
										getPlayerHeads(clan, valueRange)
												.indexOf(strings.getMessage(StringsService.PLAYER_DONATIONS)),
										false)
								+ ";" + scoreDonationsQuantity + "));" + numberFormat.format(scoreDonationsValue) + ")";

				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.SCORE_CLAN_GAMES))] = "=MULTIPLY(INT(DIVIDE("
								+ getCellName(rows.size() + 1,
										getPlayerHeads(clan, valueRange)
												.indexOf(strings.getMessage(StringsService.PLAYER_CLAN_GAMES)),
										false)
								+ ";" + scoreClanGamesPoints + "));" + numberFormat.format(scoreClanGamesValue) + ")";

				List<Integer> colsWars = getColsWars(clan, valueRange);
				if (colsWars != null && !colsWars.isEmpty()) {
					StringBuilder warCalc = new StringBuilder("=SUM(");
					for (Integer warCol : colsWars) {
						warCalc.append(getCellName(rows.size() + 1, warCol, false));
						warCalc.append(";");
					}
					warCalc.append(")");
					row[getPlayerHeads(clan, valueRange)
							.indexOf(strings.getMessage(StringsService.SCORE_WAR_ATTACKS))] = warCalc.toString();
				} else {
					row[getPlayerHeads(clan, valueRange)
							.indexOf(strings.getMessage(StringsService.SCORE_WAR_ATTACKS))] = 0;
				}

				Double scoreLeagueValue = mapTrophies
						.get(player.getLeague() != null ? player.getLeague().getName() : StringUtils.EMPTY);
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.SCORE_LEAGUE))] = scoreLeagueValue == null ? 0
								: numberFormat.format(scoreLeagueValue);

				row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.SCORE_TOTAL))] = "=SUM("
						+ String.join(";",
								getCellName(rows.size() + 1,
										getPlayerHeads(clan, valueRange)
												.indexOf(strings.getMessage(StringsService.SCORE_ATTACK)),
										false),
								getCellName(rows.size() + 1,
										getPlayerHeads(clan, valueRange)
												.indexOf(strings.getMessage(StringsService.SCORE_DONATIONS)),
										false),
								getCellName(rows.size() + 1,
										getPlayerHeads(clan, valueRange)
												.indexOf(strings.getMessage(StringsService.SCORE_CLAN_GAMES)),
										false),
								getCellName(rows.size() + 1,
										getPlayerHeads(clan, valueRange)
												.indexOf(strings.getMessage(StringsService.SCORE_WAR_ATTACKS)),
										false),
								getCellName(rows.size() + 1,
										getPlayerHeads(clan, valueRange)
												.indexOf(strings.getMessage(StringsService.SCORE_LEAGUE)),
										false))
						+ ")";

				// Controla informação de Saida
				Integer outKey = mapFixedHeaders.get(strings.getMessage(StringsService.PLAYER_OUT));
				Object outValue = StringUtils.EMPTY;
				if (oldRow.size() > mapFixedHeaders.get(strings.getMessage(StringsService.PLAYER_OUT))) {
					outValue = oldRow.get(mapFixedHeaders.get(strings.getMessage(StringsService.PLAYER_OUT)));
				}

				for (Entry<String, Integer> fixed : mapFixedHeaders.entrySet()) {
					Object oldValue = StringUtils.EMPTY;
					if (oldRow.size() > fixed.getValue()) {
						oldValue = oldRow.get(fixed.getValue());
					}
					if (oldValue != null) {
						row[getPlayerHeads(clan, valueRange).indexOf(fixed.getKey())] = oldValue;
						if (strings.getMessage(StringsService.PLAYER_IN).equalsIgnoreCase(fixed.getKey())) {
							if (StringUtils.isBlank(oldValue.toString())
									|| StringUtils.isNotBlank(outValue.toString())) {
								row[getPlayerHeads(clan, valueRange).indexOf(fixed.getKey())] = sdfDay
										.format(Calendar.getInstance().getTime());
							}
						}
						if (strings.getMessage(StringsService.PLAYER_OUT).equalsIgnoreCase(fixed.getKey())) {
							row[getPlayerHeads(clan, valueRange).indexOf(fixed.getKey())] = StringUtils.EMPTY;
						}
					}
				}

				rows.add(Arrays.asList(row));

				updateds.add(player);
			}
		}
		List<Player> newPlayers = new ArrayList<Player>(players);
		newPlayers.removeAll(updateds);

		for (Player player : newPlayers) {
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

			Object[] row = new Object[getPlayerHeads(clan, valueRange).size()];

			for (int i = 0; i < row.length; i++) {
				row[i] = StringUtils.EMPTY;
			}

			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TAG))] = player
					.getTag();
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_NAME))] = player
					.getName();
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_IN))] = sdfDay
					.format(Calendar.getInstance().getTime());
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_XP))] = player
					.getExpLevel();
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TH))] = player
					.getTownHallLevel();
			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.PLAYER_BEST_TROPHIES))] = player.getBestTrophies();
			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.PLAYER_LEAGUE))] = getPlayerLeague(player);
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TROPHIES))] = player
					.getTrophies();
			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.PLAYER_LEAGUE_OK))] = calcPlayerLeagueOk(
							player.getTownHallLevel(), player.getLeague()).name();
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_KING))] = king != null
					? king
					: StringUtils.EMPTY;
			row[getPlayerHeads(clan, valueRange).indexOf(
					strings.getMessage(StringsService.PLAYER_QUEEN))] = queen != null ? queen : StringUtils.EMPTY;
			row[getPlayerHeads(clan, valueRange).indexOf(
					strings.getMessage(StringsService.PLAYER_WARDEN))] = warden != null ? warden : StringUtils.EMPTY;
			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.PLAYER_CHAMPION))] = champion != null ? champion
							: StringUtils.EMPTY;
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_DONATIONS))] = player
					.getDonations();
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_ATTACKS))] = player
					.getAttackWins();
			if (clan instanceof ClanWar) {
				ClanWar clanWar = (ClanWar) clan;
				if (player instanceof PlayerWar) {
					PlayerWar playerWar = (PlayerWar) player;
					List<WarAttack> wins = new ArrayList<WarAttack>();
					if (playerWar.getAttacks() != null) {
						wins = playerWar.getAttacks().stream().filter(attack -> attack.getStars() > 0)
								.collect(Collectors.toList());
						double scoreStars = (wins.stream().map(win -> win.getStars()).reduce(0, Integer::sum)
								* scoreWarAttacksValue)
								+ ((playerWar.getAttacks().size() - ConstantsCOC.WAR_NUMBER_ATTACKS)
										* scoreWarNoAttacksValue);

						row[getPlayerHeads(clan, valueRange)
								.indexOf(getCurrentWarName(clanWar.getWar().getStartTime()))] = scoreStars;

					} else {
						row[getPlayerHeads(clan, valueRange).indexOf(getCurrentWarName(
								clanWar.getWar().getStartTime()))] = (0 - ConstantsCOC.WAR_NUMBER_ATTACKS)
										* scoreWarNoAttacksValue;
					}

				} else {
					row[getPlayerHeads(clan, valueRange)
							.indexOf(getCurrentWarName(clanWar.getWar().getStartTime()))] = StringUtils.EMPTY;
				}
			}

			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.PLAYER_CLAN_GAMES_INIT))] = player
							.progressValue(ConstantsCOC.GAMES_PONTUATION);
			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.PLAYER_CLAN_GAMES))] = "=MINUS("
							+ player.progressValue(ConstantsCOC.GAMES_PONTUATION) + ";"
							+ getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.PLAYER_CLAN_GAMES_INIT)),
									false)
							+ ")";

			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.SCORE_ATTACK))] = "=MULTIPLY("
							+ String.join(";",
									getCellName(rows.size() + 1,
											getPlayerHeads(clan, valueRange)
													.indexOf(strings.getMessage(StringsService.PLAYER_ATTACKS)),
											false),
									numberFormat.format(scoreAttacksValue))
							+ ")";

			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.SCORE_DONATIONS))] = "=MULTIPLY(INT(DIVIDE("
							+ getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.PLAYER_DONATIONS)),
									false)
							+ ";" + scoreDonationsQuantity + "));" + numberFormat.format(scoreDonationsValue) + ")";

			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.SCORE_CLAN_GAMES))] = "=MULTIPLY(INT(DIVIDE("
							+ getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.PLAYER_CLAN_GAMES)),
									false)
							+ ";" + scoreClanGamesPoints + "));" + numberFormat.format(scoreClanGamesValue) + ")";

			List<Integer> colsWars = getColsWars(clan, valueRange);
			if (colsWars != null && !colsWars.isEmpty()) {
				StringBuilder warCalc = new StringBuilder("=SUM(");
				for (Integer warCol : colsWars) {
					warCalc.append(getCellName(rows.size() + 1, warCol, false));
					warCalc.append(";");
				}
				warCalc.append(")");
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.SCORE_WAR_ATTACKS))] = warCalc.toString();
			} else {
				row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.SCORE_WAR_ATTACKS))] = 0;
			}

			Double scoreLeagueValue = mapTrophies
					.get(player.getLeague() != null ? player.getLeague().getName() : StringUtils.EMPTY);
			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.SCORE_LEAGUE))] = scoreLeagueValue == null ? 0
							: numberFormat.format(scoreLeagueValue);

			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.SCORE_TOTAL))] = "=SUM("
					+ String.join(";",
							getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.SCORE_ATTACK)),
									false),
							getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.SCORE_DONATIONS)),
									false),
							getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.SCORE_CLAN_GAMES)),
									false),
							getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.SCORE_WAR_ATTACKS)),
									false),
							getCellName(rows.size() + 1, getPlayerHeads(clan, valueRange)
									.indexOf(strings.getMessage(StringsService.SCORE_LEAGUE)), false))
					+ ")";

			rows.add(Arrays.asList(row));
		}

		// Atualiza informação dos que sairam
		for (List<Object> oldRow : outs) {

			Object[] row = new Object[getPlayerHeads(clan, valueRange).size()];

			for (int i = 0; i < row.length; i++) {
				row[i] = StringUtils.EMPTY;
			}

			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TAG))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TAG)));

			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_NAME))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_NAME)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_XP))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_XP)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TH))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TH)));
			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.PLAYER_BEST_TROPHIES))] = oldRow
							.get(getPlayerHeads(clan, valueRange)
									.indexOf(strings.getMessage(StringsService.PLAYER_BEST_TROPHIES)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_LEAGUE))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_LEAGUE)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TROPHIES))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_TROPHIES)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_LEAGUE_OK))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_LEAGUE_OK)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_KING))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_KING)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_QUEEN))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_QUEEN)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_WARDEN))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_WARDEN)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_CHAMPION))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_CHAMPION)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_DONATIONS))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_DONATIONS)));
			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_ATTACKS))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_ATTACKS)));

			if (clan instanceof ClanWar) {
				ClanWar clanWar = (ClanWar) clan;
				row[getPlayerHeads(clan, valueRange)
						.indexOf(getCurrentWarName(clanWar.getWar().getStartTime()))] = StringUtils.EMPTY;
			}

			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.PLAYER_CLAN_GAMES))] = oldRow
					.get(getPlayerHeads(clan, valueRange)
							.indexOf(strings.getMessage(StringsService.PLAYER_CLAN_GAMES)));

			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.SCORE_ATTACK))] = "=MULTIPLY("
							+ String.join(";",
									getCellName(rows.size() + 1,
											getPlayerHeads(clan, valueRange)
													.indexOf(strings.getMessage(StringsService.PLAYER_ATTACKS)),
											false),
									numberFormat.format(scoreAttacksValue))
							+ ")";

			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.SCORE_DONATIONS))] = "=MULTIPLY(INT(DIVIDE("
							+ getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.PLAYER_DONATIONS)),
									false)
							+ ";" + scoreDonationsQuantity + "));" + numberFormat.format(scoreDonationsValue) + ")";

			row[getPlayerHeads(clan, valueRange)
					.indexOf(strings.getMessage(StringsService.SCORE_CLAN_GAMES))] = "=MULTIPLY(INT(DIVIDE("
							+ getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.PLAYER_CLAN_GAMES)),
									false)
							+ ";" + scoreClanGamesPoints + "));" + numberFormat.format(scoreClanGamesValue) + ")";

			List<Integer> colsWars = getColsWars(clan, valueRange);
			if (colsWars != null && !colsWars.isEmpty()) {
				StringBuilder warCalc = new StringBuilder("=SUM(");
				for (Integer warCol : colsWars) {
					warCalc.append(getCellName(rows.size() + 1, warCol, false));
					warCalc.append(";");
				}
				warCalc.append(")");
				row[getPlayerHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.SCORE_WAR_ATTACKS))] = warCalc.toString();
			} else {
				row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.SCORE_WAR_ATTACKS))] = 0;
			}

			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.SCORE_LEAGUE))] = oldRow
					.get(getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.SCORE_LEAGUE)));

			row[getPlayerHeads(clan, valueRange).indexOf(strings.getMessage(StringsService.SCORE_TOTAL))] = "=SUM("
					+ String.join(";",
							getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.SCORE_ATTACK)),
									false),
							getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.SCORE_DONATIONS)),
									false),
							getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.SCORE_CLAN_GAMES)),
									false),
							getCellName(rows.size() + 1,
									getPlayerHeads(clan, valueRange)
											.indexOf(strings.getMessage(StringsService.SCORE_WAR_ATTACKS)),
									false),
							getCellName(rows.size() + 1, getPlayerHeads(clan, valueRange)
									.indexOf(strings.getMessage(StringsService.SCORE_LEAGUE)), false))
					+ ")";

			for (Entry<String, Integer> fixed : mapFixedHeaders.entrySet()) {
				Object oldValue = StringUtils.EMPTY;
				if (oldRow.size() > fixed.getValue()) {
					oldValue = oldRow.get(fixed.getValue());
				}
				if (oldValue != null) {
					row[getPlayerHeads(clan, valueRange).indexOf(fixed.getKey())] = oldValue;
					if (strings.getMessage(StringsService.PLAYER_OUT).equalsIgnoreCase(fixed.getKey())) {
						if (StringUtils.isBlank(oldValue.toString())) {
							row[getPlayerHeads(clan, valueRange).indexOf(fixed.getKey())] = sdfDay
									.format(Calendar.getInstance().getTime());
						}
					}
				}
			}

			rows.add(Arrays.asList(row));
		}

		// Limpa celulas de players que sairam
		if (valueRange != null && valueRange.getValues() != null && valueRange.getValues().size() > rows.size()) {
			for (int j = rows.size() - 1; j < valueRange.getValues().size(); j++) {
				Object[] row = new Object[getPlayerHeads(clan, valueRange).size()];

				for (int i = 0; i < row.length; i++) {
					row[i] = StringUtils.EMPTY;
				}
				rows.add(Arrays.asList(row));
			}
		}

		ValueRange bodyValues = new ValueRange().setValues(rows).setRange(sheetName);

		return bodyValues;
	}

	private EnumOkNok calcPlayerLeagueOk(Integer townHallLevel, League league) {
		if (league == null) {
			return EnumOkNok.NOK;
		}
		if (!mapMinimalLeagues.containsKey(townHallLevel)) {
			return EnumOkNok.NOK;
		}
		Double minimal = mapTrophies.get(mapMinimalLeagues.get(townHallLevel));
		Double current = mapTrophies.get(league.getName());
		return minimal > current ? EnumOkNok.NOK : EnumOkNok.OK;
	}

	private List<Integer> getColsWars(Clan clan, ValueRange valueRange) {
		List<Integer> result = new ArrayList<Integer>();
		for (Object header : getPlayerHeads(clan, valueRange)) {
			if (header.toString().toUpperCase()
					.startsWith(strings.getMessage(StringsService.PLAYER_WAR_DAY).toUpperCase())) {
				result.add(getPlayerHeads(clan, valueRange).indexOf(header));
			}
		}
		return result;
	}

	private List<Object> getPlayerHeads(Clan clan, ValueRange valueRange) {
		if (playerHeaders == null || playerHeaders.isEmpty()) {

			if (valueRange == null) {

				playerHeaders = new ArrayList<Object>();
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_TAG));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_NAME));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_XP));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_TH));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_BEST_TROPHIES));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_CLAN_GAMES_INIT));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_LEAGUE));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_TROPHIES));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_LEAGUE_OK));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_KING));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_QUEEN));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_WARDEN));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_CHAMPION));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_DONATIONS));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_ATTACKS));
				if (clan instanceof ClanWar) {
					playerHeaders.add(getCurrentWarName(((ClanWar) clan).getWar().getStartTime()));
				}
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_CLAN_GAMES));
				playerHeaders.add(strings.getMessage(StringsService.SCORE_ATTACK));
				playerHeaders.add(strings.getMessage(StringsService.SCORE_DONATIONS));
				playerHeaders.add(strings.getMessage(StringsService.SCORE_CLAN_GAMES));
				playerHeaders.add(strings.getMessage(StringsService.SCORE_WAR_ATTACKS));
				playerHeaders.add(strings.getMessage(StringsService.SCORE_LEAGUE));
				playerHeaders.add(strings.getMessage(StringsService.SCORE_TOTAL));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_IN));
				playerHeaders.add(strings.getMessage(StringsService.PLAYER_OUT));
			} else {
				List<List<Object>> rows = valueRange.getValues();
				int row = 0;
				boolean foundedHeader = false;
				for (List<Object> cols : rows) {
					for (Object col : cols) {
						if (strings.getMessage(StringsService.PLAYER_TAG).equalsIgnoreCase(col.toString())) {
							foundedHeader = true;
							break;
						}
					}
					if (foundedHeader) {
						break;
					}
					row++;
				}

				boolean isWar = false;
				int col = 0;
				String currentWar = null;
				boolean foundWarHeader = false;
				for (Object header : rows.get(row)) {
					if (clan instanceof ClanWar && header.toString().toUpperCase()
							.startsWith(strings.getMessage(StringsService.PLAYER_WAR_DAY).toUpperCase())) {
						currentWar = getCurrentWarName(((ClanWar) clan).getWar().getStartTime());
						if (currentWar.equalsIgnoreCase(header.toString())) {
							isWar = false;
						} else {
							isWar = true;
						}
						foundWarHeader = true;
					} else if (isWar) {
						playerHeaders.add(currentWar);
						isWar = false;
					}

					if (strings.getMessage(StringsService.PLAYER_TAG).equalsIgnoreCase(header.toString())) {
						colPlayerTag = col;
					}

					updateReferenceCols(header.toString(), col, currentWar);

					playerHeaders.add(header.toString());
					col++;
				}
			}
		}
		return playerHeaders;
	}

	private void updateReferenceCols(String header, int col, String currentWar) {
		if (header.equalsIgnoreCase(currentWar)) {
			return;
		} else if (strings.getMessage(StringsService.PLAYER_CLAN_GAMES_INIT).equalsIgnoreCase(header)) {
			mapFixedHeaders.put(header, col);
		} else if (strings.getMessage(StringsService.PLAYER_IN).equalsIgnoreCase(header)) {
			mapFixedHeaders.put(header, col);
		} else if (strings.getMessage(StringsService.PLAYER_OUT).equalsIgnoreCase(header)) {
			mapFixedHeaders.put(header, col);
		} else if (header.toUpperCase().startsWith(strings.getMessage(StringsService.PLAYER_WAR_DAY).toUpperCase())) {
			mapFixedHeaders.put(header, col);
		}
	}

	private String getCurrentWarName(Date startTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(startTime);
		return strings.getMessage(StringsService.PLAYER_WAR_DAY) + "-" + cal.get(Calendar.DAY_OF_MONTH);
	}

	private void exportPush(Clan clan) throws IOException, GeneralSecurityException {

		String sheetName = sheetNamePush + "-" + leagueSeasonService.getCurrentSeason() + "-" + clan.getTag();

		ValueRange valueRange = null;
		try {
			valueRange = getService().spreadsheets().values().get(pushSheet, sheetName).execute();
			valueRange = updatePushValues(getService(), clan, sheetName, valueRange);
		} catch (IOException e) {
			valueRange = createPushValues(getService(), sheetName, clan);
		}

		UpdateValuesResponse result = getService().spreadsheets().values()
				.update(pushSheet, valueRange.getRange(), valueRange).setValueInputOption("USER_ENTERED").execute();
		System.out.printf("%d cells updated.", result.getUpdatedCells());

	}

	private Sheets getService() throws GeneralSecurityException, IOException {
		if (service == null) {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
		}

		return service;
	}

	private String getRangeCell(int startRow, int startColumn, int endRow, int endColumn, boolean fixed) {
		return getCellName(startRow, startColumn, fixed) + ":" + getCellName(endRow, endColumn, fixed);

	}

	private String getCellName(int startRow, int startColumn, boolean fixed) {
		if (fixed) {
			return "$" + toAlphabetic(startColumn) + "$" + startRow;
		}
		return toAlphabetic(startColumn) + startRow;
	}

	private ValueRange createPushValues(Sheets service, String sheetName, Clan clan) throws IOException {
		List<Request> requests = new ArrayList<>();
		requests.add(new Request()
				.setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().set("title", sheetName))));

		BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
		service.spreadsheets().batchUpdate(pushSheet, body).execute();

		return updatePushValues(service, clan, sheetName, null);
	}

	private String getPlayerLeague(Player player) {
		return player.getSmallImage() != null ? "=IMAGE(\"" + player.getSmallImage() + "\")" : StringUtils.EMPTY;
	}

	private ValueRange updatePushValues(Sheets service, Clan clan, String sheetName, ValueRange valueRange) {
		List<Player> players = clan.getMembers();
		Collections.sort(players, new Comparator<Player>() {

			@Override
			public int compare(Player o1, Player o2) {
				int comp = o2.getTrophies().compareTo(o1.getTrophies());
				return comp == 0 ? o1.getExpLevel().compareTo(o2.getExpLevel()) : comp;
			}
		});

		List<List<Object>> rows = new ArrayList<List<Object>>();
		rows.add(Arrays.asList("=IMAGE(\"" + clan.getShield() + "\")", clan.getName()));
		rows.add(Arrays
				.asList(strings.getMessage(StringsService.UPDATED, sdfTime.format(Calendar.getInstance().getTime()))));
		rows.add(getPushHeads(clan, valueRange));
		Map<String, Map<String, Map<String, String>>> mapPush = new HashMap<String, Map<String, Map<String, String>>>();

		if (valueRange != null) {
			List<List<Object>> oldRows = valueRange.getValues();
			boolean foundedHeader = false;
			// tag,date,header,value
			for (List<Object> oldRow : oldRows) {
				if (!foundedHeader) {
					if (!oldRow.contains(strings.getMessage(StringsService.PLAYER_TAG))) {
						continue;
					}
					foundedHeader = true;
					continue;
				}

				String playerTag = oldRow.get(colPushPlayerTag) != null ? oldRow.get(colPushPlayerTag).toString() : null;
				if (!mapPush.containsKey(playerTag)) {
					mapPush.put(playerTag, new HashMap<String, Map<String, String>>());
				}

				Map<String, Map<String, String>> mapPlayer = mapPush.get(playerTag);
				String valueDate = getOldPushValue(oldRow, clan, valueRange,
						strings.getMessage(StringsService.PLAYER_PUSH_DATE));
				if (!mapPlayer.containsKey(valueDate)) {
					mapPlayer.put(valueDate, new HashMap<String, String>());
				}

				Map<String, String> mapOtherValues = mapPlayer.get(valueDate);
				for (Object header : getPushHeads(clan, valueRange)) {
					if (!strings.getMessage(StringsService.PLAYER_TAG).equals(header.toString())
							&& !strings.getMessage(StringsService.PLAYER_PUSH_DATE).equals(header.toString())) {
						mapOtherValues.put(header.toString(),
								getOldPushValue(oldRow, clan, valueRange, header.toString()));
					}
				}
			}
		}
		
		String currentDate = sdfDay.format(Calendar.getInstance().getTime());
		for (Player player : players) {
			if (!mapPush.containsKey(player.getTag())) {
				mapPush.put(player.getTag(), new HashMap<String, Map<String, String>>());
			}

			Map<String, Map<String, String>> mapPlayer = mapPush.get(player.getTag());
			if (!mapPlayer.containsKey(currentDate)) {
				mapPlayer.put(currentDate, new HashMap<String, String>());
			}

			Map<String, String> mapOtherValues = mapPlayer.get(currentDate);
			
			mapOtherValues.put(strings.getMessage(StringsService.PLAYER_NAME), player.getName());
			mapOtherValues.put(strings.getMessage(StringsService.PLAYER_XP), player.getExpLevel()!= null?player.getExpLevel().toString():StringUtils.EMPTY);
			mapOtherValues.put(strings.getMessage(StringsService.PLAYER_TH), player.getTownHallLevel()!= null?player.getTownHallLevel().toString():StringUtils.EMPTY);
			mapOtherValues.put(strings.getMessage(StringsService.PLAYER_BEST_TROPHIES), player.getBestTrophies()!= null?player.getBestTrophies().toString():StringUtils.EMPTY);
			mapOtherValues.put(strings.getMessage(StringsService.PLAYER_TROPHIES), player.getTrophies()!= null?player.getTrophies().toString():StringUtils.EMPTY);
							
		}

		for (Entry<String, Map<String, Map<String, String>>> entryPlayer : mapPush.entrySet()) {
			for (Entry<String, Map<String, String>> entryDate : entryPlayer.getValue().entrySet()) {
				Object[] row = new Object[getPushHeads(clan, valueRange).size()];
				for (int i = 0; i < row.length; i++) {
					row[i] = StringUtils.EMPTY;
				}
				row[getPushHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_TAG))] = entryPlayer.getKey();
				row[getPushHeads(clan, valueRange)
						.indexOf(strings.getMessage(StringsService.PLAYER_PUSH_DATE))] = entryDate.getKey();
				for (Entry<String, String> entryOthers : entryDate.getValue().entrySet()) {
					row[getPushHeads(clan, valueRange).indexOf(entryOthers.getKey())] = entryOthers.getValue();
				}
				rows.add(Arrays.asList(row));
			}
		}
		
		ValueRange bodyValues = new ValueRange().setValues(rows).setRange(sheetName);
		return bodyValues;

	}

	private String getOldPushValue(List<Object> oldRow, Clan clan, ValueRange valueRange, String header) {
		int index = getPushHeads(clan, valueRange).indexOf(header);
		if (oldRow.size() > index) {
			return oldRow.get(index).toString();
		}
		return StringUtils.EMPTY;
	}

	private List<Object> getPushHeads(Clan clan, ValueRange valueRange) {
		if (playerPush == null || playerPush.isEmpty()) {
			playerPush = new ArrayList<Object>();
			
			if (valueRange == null) {
				playerPush.add(strings.getMessage(StringsService.PLAYER_TAG));
				playerPush.add(strings.getMessage(StringsService.PLAYER_NAME));
				playerPush.add(strings.getMessage(StringsService.PLAYER_PUSH_DATE));
				playerPush.add(strings.getMessage(StringsService.PLAYER_XP));
				playerPush.add(strings.getMessage(StringsService.PLAYER_TH));
				playerPush.add(strings.getMessage(StringsService.PLAYER_TROPHIES));
				playerPush.add(strings.getMessage(StringsService.PLAYER_BEST_TROPHIES));
			} else {
				List<List<Object>> rows = valueRange.getValues();
				int row = 0;
				boolean foundedHeader = false;
				for (List<Object> cols : rows) {
					for (Object col : cols) {
						if (strings.getMessage(StringsService.PLAYER_TAG).equalsIgnoreCase(col.toString())) {
							foundedHeader = true;
							break;
						}
					}
					if (foundedHeader) {
						break;
					}
					row++;
				}

				for (Object header : rows.get(row)) {
					playerPush.add(header.toString());
				}
			}
			colPushPlayerTag = playerPush.indexOf(strings.getMessage(StringsService.PLAYER_TAG));
		}
		return playerPush;
	}

	public static String toAlphabetic(int i) {
		if (i < 0) {
			return "-" + toAlphabetic(-i - 1);
		}

		int quot = i / 26;
		int rem = i % 26;
		char letter = (char) ((int) 'A' + rem);
		if (quot == 0) {
			return "" + letter;
		} else {
			return toAlphabetic(quot - 1) + letter;
		}
	}

	public void saveWar(Clan clan) {
		if (clan == null || !(clan instanceof ClanWar) || ((ClanWar) clan).getWar() == null
				|| EnumWarState.notInWar.equals(((ClanWar) clan).getWar().getState())) {
			return;
		}

		try {
			System.out.println(warSheet);
			exportWar((ClanWar) clan);
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}

	}

	private void exportWar(ClanWar clan) throws GeneralSecurityException, IOException {
		String sheetName = sheetNameWar + "-" + sdfDayWar.format(clan.getWar().getStartTime()) + "-" + clan.getTag();

		ValueRange valueRange = null;
		try {
			valueRange = getService().spreadsheets().values().get(warSheet, sheetName).execute();
			valueRange = updateWarValues(getService(), clan, sheetName, valueRange);
		} catch (IOException e) {
			valueRange = createWarValues(getService(), sheetName, clan);
		}

//		UpdateValuesResponse result = getService().spreadsheets().values()
//				.update(warSheet, valueRange.getRange(), valueRange).setValueInputOption("USER_ENTERED").execute();
//		System.out.printf("%d cells updated.", result.getUpdatedCells());

	}

	private ValueRange createWarValues(Sheets service, String sheetName, ClanWar clan) throws IOException {
		List<Request> requests = new ArrayList<>();
		requests.add(new Request()
				.setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().set("title", sheetName))));

		BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
		service.spreadsheets().batchUpdate(warSheet, body).execute();

		return updateWarValues(service, clan, sheetName, null);
	}

	private ValueRange updateWarValues(Sheets service, Clan clan, String sheetName, ValueRange valueRange) {
		return null;
	}

}
