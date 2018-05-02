package labs.module08309.acw;

import java.io.Serializable;

/**
 * Created by Toby on 23/04/2016.
 */
class LevelObject implements Serializable {
    public String getM_LevelName() {
        return m_LevelName;
    }

    public void setM_LevelName(String m_LevelName) {
        this.m_LevelName = m_LevelName;
    }

    public String getM_LevelTime() {
        return m_LevelTime;
    }

    public void setM_LevelTime(String m_LevelTime) {
        this.m_LevelTime = m_LevelTime;
    }

    public int getM_LevelScore() {
        return m_LevelScore;
    }

    public void setM_LevelScore(int m_LevelScore) {
        this.m_LevelScore = m_LevelScore;
    }

    public Boolean getM_IsComplete() {
        return m_IsComplete;
    }

    public void setM_IsComplete(Boolean m_IsComplete) {
        this.m_IsComplete = m_IsComplete;
    }

    public int getM_Rows() {
        return m_Rows;
    }

    public void setM_Rows(int m_Rows) {
        this.m_Rows = m_Rows;
    }

    public int getM_Columns() {
        return m_Columns;
    }

    public void setM_Columns(int m_Columns) {
        this.m_Columns = m_Columns;
    }

    private String m_LevelName;
    private int m_LevelScore;
    private int m_Rows;
    private int m_Columns;
    private String m_LevelTime;
    private Boolean m_IsComplete;


    public LevelObject(String pLevelName,int pLevelScore,String pLevelTime,Boolean pIsComplete,
                       int pRows, int pColumns){
        m_LevelName = pLevelName;
        m_LevelScore = pLevelScore;
        m_LevelTime = pLevelTime;
        m_IsComplete = pIsComplete;
        m_Rows = pRows;
        m_Columns = pColumns;
    }
}
