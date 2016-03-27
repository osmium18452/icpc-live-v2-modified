package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.datapassing.StandingsData;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.ProblemInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFContestInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class BigStandingsWidget extends Widget {
    private static final double V = 0.01;
    private static int STANDING_TIME = 5000;
    private static int TOP_PAGE_STANDING_TIME = 10000;
    private static final int MOVING_TIME = 500;
    private static final int BIG_SPACE_COUNT = 6;
    public static int PERIOD = STANDING_TIME + MOVING_TIME;

    private static final double SPACE_Y = 0.1;
    private static final double SPACE_X = 0.1;
    private static final double NAME_WIDTH = 6;
    private static final double RANK_WIDTH = 1.6;
    private static final double TOTAL_WIDTH = 1.6;
    private static final double PENALTY_WIDTH = 2.3;

    private final int plateHeight;
    private final int spaceY;
    private final int spaceX;

    private final int nameWidth;
    private final int rankWidth;
    private final int totalWidth;
    private final int penaltyWidth;

    public int length;

    private final Font font;

    int timer;
    int start;
    final int baseX;
    int baseY;
    final int width;
    final boolean controlled;
    final int teamsOnPage;

    private ContestInfo contestData;
    private StandingsData.OptimismLevel optimismLevel = StandingsData.OptimismLevel.NORMAL;

    double[] currentTeamPositions;
    double[] desiredTeamPositions;

    public BigStandingsWidget(int baseX, int baseY, int width, int plateHeight, long updateWait, int teamsOnPage, boolean controlled) {
        super(updateWait);
        last = System.currentTimeMillis();

        this.baseX = baseX;
        this.baseY = baseY;
        this.width = width;
        this.plateHeight = plateHeight;
        this.teamsOnPage = teamsOnPage;
        this.controlled = controlled;

        if (!controlled) {
            setVisibilityState(1);
            setVisible(true);
        }

        spaceX = (int) Math.round(plateHeight * SPACE_X);
        spaceY = (int) Math.round(plateHeight * SPACE_Y);

        nameWidth = (int) Math.round(NAME_WIDTH * plateHeight);
        rankWidth = (int) Math.round(RANK_WIDTH * plateHeight);
        totalWidth = (int) Math.round(TOTAL_WIDTH * plateHeight);
        penaltyWidth = (int) Math.round(PENALTY_WIDTH * plateHeight);

        this.updateWait = updateWait;

        font = Font.decode("Open Sans " + (int) (plateHeight * 0.7));
    }

    public void setState(StandingsData.StandingsType type) {
        switch (type) {
            case ONE_PAGE:
                length = Math.min(teamsOnPage, contestData.getTeamsNumber());
                start = 0;
                timer = -Integer.MAX_VALUE;
                break;
            case TWO_PAGES:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 10000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                length = Math.min(teamsOnPage * 2, contestData.getTeamsNumber());
                start = 0;
                timer = 0;
                break;
            case ALL_PAGES:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 5000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                length = contestData.getTeamsNumber();
                start = 0;
                timer = -TOP_PAGE_STANDING_TIME + STANDING_TIME;
        }
        setVisible(true);
    }

    public static long totalTime(StandingsData.StandingsType type, int teamNumber) {
        int pages = teamNumber / 20;
        switch (type) {
            case ONE_PAGE:
                return Integer.MAX_VALUE;
            case TWO_PAGES:
                return TOP_PAGE_STANDING_TIME + STANDING_TIME + MOVING_TIME;
            default:
                return (pages - 1) * (STANDING_TIME + MOVING_TIME) + TOP_PAGE_STANDING_TIME;
        }
    }

    private long lastChange;

    protected void updateImpl(Data data) {
        if (data.standingsData.isStandingsVisible() && data.standingsData.isBig()) {
            if (lastChange != data.standingsData.getStandingsTimestamp()) {
                if (!isVisible()) {
                    setState(data.standingsData.getStandingsType());
                }
            }
        } else {
            setVisible(false);
        }
        lastChange = data.standingsData.getStandingsTimestamp();
        optimismLevel = data.standingsData.optimismLevel;
    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        contestData = Preparation.eventsLoader.getContestData();
        if (contestData == null) {
            return;
        }

        if (controlled) {
            update();
        }

        int dt = updateVisibilityState();

        if (!isVisible() && visibilityState == 0) {
            currentTeamPositions = null;
            return;
        }

        g = (Graphics2D) g.create();
        g.translate(baseX, baseY);

        TeamInfo[] standings;
        if (contestData instanceof WFContestInfo) {
            standings = ((WFContestInfo) contestData).getStandings(optimismLevel);
        } else {
            standings = contestData.getStandings();
        }

        if (contestData == null || standings == null) return;
        length = Math.min(contestData.getTeamsNumber(), standings.length);

        if (desiredTeamPositions == null || desiredTeamPositions.length != contestData.getTeamsNumber() + 1) {
            desiredTeamPositions = new double[contestData.getTeamsNumber() + 1];
        }
        {
            int i = 0;
            for (TeamInfo teamInfo : standings) {
                desiredTeamPositions[teamInfo.getId()] = i;
                i++;
            }
        }
        if (currentTeamPositions == null || currentTeamPositions.length != contestData.getTeamsNumber() + 1) {
            currentTeamPositions = desiredTeamPositions.clone();
        }

        if (visibilityState > 0) {
            if (isVisible()) {
                timer = timer + dt;
                if (timer >= PERIOD) {
                    timer -= PERIOD;
                    start += teamsOnPage;
                    if (start >= length && !controlled) {
                        start = 0;
                        timer = -TOP_PAGE_STANDING_TIME + STANDING_TIME;
                    }
                }
            }
            double start = this.start;
            if (timer >= STANDING_TIME) {
                if (start + teamsOnPage >= length && controlled) {
                    setVisible(false);
                } else {
                    double t = (timer - STANDING_TIME) * 1.0 / MOVING_TIME;
                    start -= ((2 * t * t * t - 3 * t * t) * teamsOnPage);
                }
            }

            drawHead(g, spaceX, 0, contestData.getProblemsNumber());
            g = (Graphics2D) g.create();
            int initY = plateHeight + BIG_SPACE_COUNT * spaceY;
            g.clip(new Rectangle(-plateHeight, initY, this.width + 2 * plateHeight, (spaceY + plateHeight) * teamsOnPage + initY));

            int lastProblems = -1;
            boolean bright = true;

            for (int i = standings.length - 1; i >= 0; i--) {
                TeamInfo teamInfo = standings[i];
                if (teamInfo.getSolvedProblemsNumber() != lastProblems) {
                    lastProblems = teamInfo.getSolvedProblemsNumber();
                    bright = !bright;
                }
                int id = teamInfo.getId();
                double dp = dt * V;
                if (Math.abs(currentTeamPositions[id] - desiredTeamPositions[id]) < dp) {
                    currentTeamPositions[id] = desiredTeamPositions[id];
                } else {
                    if (desiredTeamPositions[id] < currentTeamPositions[id]) {
                        currentTeamPositions[id] -= dp;
                    } else {
                        currentTeamPositions[id] += dp;
                    }
                    if (currentTeamPositions[id] < start - 1 && desiredTeamPositions[id] > start - 1) {
                        currentTeamPositions[id] = start - 1;
                    }
                    if (currentTeamPositions[id] > start + teamsOnPage && desiredTeamPositions[id] < start + teamsOnPage) {
                        currentTeamPositions[id] = start + teamsOnPage;
                    }
                }
                double yy = currentTeamPositions[id] - start;
                if (yy > -1 && yy < teamsOnPage) {
                    drawFullTeamPane(g, teamInfo, spaceX, initY + (int) (yy * (plateHeight + spaceY)), bright);
                }
            }

        } else {
            timer = -TOP_PAGE_STANDING_TIME;
            start = 0;
        }
    }

    private void drawHead(Graphics2D g, int x, int y, int problemsNumber) {
        g.setFont(font);
        int problemWidth = problemWidth(problemsNumber);
        drawTextInRect(g, "Current Standings", x, y,
                rankWidth + nameWidth + spaceX, plateHeight,
                POSITION_CENTER, ACCENT_COLOR, Color.white, visibilityState);
        x += rankWidth + nameWidth + 2 * spaceX;
        for (int i = 0; i < problemsNumber; i++) {
            ProblemInfo problem = contestData.problems.get(i);
            drawTextInRect(g, problem.letter, x, y, problemWidth, plateHeight,
                    POSITION_CENTER, MAIN_COLOR, Color.white, visibilityState);
            x += problemWidth + spaceX;
        }
    }

    private void drawFullTeamPane(Graphics2D g, TeamInfo team, int x, int y, boolean bright) {
        Color mainColor = MAIN_COLOR;
        if (bright) mainColor = mainColor.brighter();

        Font font = this.font;
        g.setFont(font);
        Color color = getTeamRankColor(team);
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y,
                rankWidth, plateHeight, POSITION_CENTER,
                color, Color.white, visibilityState);

        x += rankWidth + spaceX;

        String name = team.getShortName();//getShortName(g, team.getShortName());
        drawTextInRect(g, name, x, y,
                nameWidth, plateHeight, POSITION_LEFT,
                mainColor, Color.white, visibilityState);

        x += nameWidth + spaceX;

        int problemWidth = problemWidth(contestData.getProblemsNumber());
        for (int i = 0; i < contestData.getProblemsNumber(); i++) {
            String status = team.getShortProblemState(i);
            Color statusColor = status.startsWith("+") ? GREEN_COLOR :
                    status.startsWith("?") ? YELLOW_COLOR :
                            status.startsWith("-") ? RED_COLOR :
                                    MAIN_COLOR;
            if (bright) statusColor = statusColor.brighter();

            if (status.startsWith("-")) status = "\u2212" + status.substring(1);
            drawTextInRect(g, status, x, y,
                    problemWidth, plateHeight, POSITION_CENTER, statusColor, Color.WHITE, visibilityState);
            x += problemWidth + spaceX;
        }

        g.setFont(font);
        drawTextInRect(g, "" + team.getSolvedProblemsNumber(), x, y, totalWidth,
                plateHeight, POSITION_CENTER, mainColor, Color.white, visibilityState);
        x += totalWidth + spaceX;
        drawTextInRect(g, "" + team.getPenalty(), x, y, penaltyWidth,
                plateHeight, POSITION_CENTER, mainColor, Color.white, visibilityState);
    }

    private int problemWidth(int problemsNumber) {
        return (int) Math.round((width - rankWidth - nameWidth - totalWidth - penaltyWidth - 3 * spaceX) * 1.0 / problemsNumber - spaceX);
    }

    public void alignBottom(int y) {
        baseY = y - teamsOnPage * (plateHeight + spaceY) - BIG_SPACE_COUNT * spaceY - plateHeight;
    }
}
