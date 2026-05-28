import json, random

els = []
cnt = 0
def nid():
    global cnt; cnt += 1
    return f"v2-{cnt:03d}"
def nidx():
    # ascending lexicographic fractional index
    return f"a{cnt:04d}"
def seed(): return random.randint(1, 2**31)

FRAME_ID = None  # no Excalidraw frame -> no clipping; "screen" is a plain rectangle

def base(t, x, y, w, h, frame=True, **extra):
    e = dict(id=nid(), type=t, x=x, y=y, width=w, height=h, angle=0,
             strokeColor="#1e1e1e", backgroundColor="transparent", fillStyle="solid",
             strokeWidth=1, strokeStyle="solid", roughness=1, opacity=100,
             groupIds=[], frameId=(FRAME_ID if frame else None), roundness=None,
             seed=seed(), versionNonce=seed(), isDeleted=False, boundElements=[],
             updated=1779884065191, link=None, locked=False, version=1, index=nidx())
    e.update(extra); return e

def rect(x,y,w,h, bg="#ffffff", stroke="#e4e4e7", frame=True, sw=1, rough=1, rounded=True):
    e = base("rectangle",x,y,w,h,frame=frame, backgroundColor=bg, strokeColor=stroke,
             strokeWidth=sw, roughness=rough)
    if rounded: e["roundness"]={"type":3}
    els.append(e); return e

def text(x,y,s,fs=15,color="#1e1e1e",frame=True,align="left",ff=5):
    lines = s.split("\n")
    w = max(6, int(max(len(ln) for ln in lines)*fs*0.6))
    h = int(len(lines)*fs*1.25)
    e = base("text",x,y,w,h,frame=frame, strokeColor=color, roughness=0,
             fontSize=fs, fontFamily=ff, text=s, textAlign=align, verticalAlign="top",
             baseline=int(fs*0.85), containerId=None, originalText=s, lineHeight=1.25, autoResize=True)
    els.append(e); return e

def ellipse(cx,cy,d,bg,stroke=None,frame=True):
    e = base("ellipse",cx-d/2,cy-d/2,d,d,frame=frame, backgroundColor=bg,
             strokeColor=stroke or bg, roundness={"type":3})
    els.append(e); return e

def line(x,y,pts,color="#e4e4e7",frame=True,sw=1):
    w=max(p[0] for p in pts)-min(p[0] for p in pts)
    h=max(p[1] for p in pts)-min(p[1] for p in pts)
    e=base("line",x,y,w,h,frame=frame,strokeColor=color,strokeWidth=sw,roughness=1,
           points=[list(p) for p in pts], lastCommittedPoint=None,
           startBinding=None,endBinding=None,startArrowhead=None,endArrowhead=None,polygon=False)
    els.append(e); return e

def arrow(x,y,pts,color="#e03131",frame=False,sw=1.5):
    w=abs(pts[-1][0]-pts[0][0]); h=abs(pts[-1][1]-pts[0][1])
    e=base("arrow",x,y,max(w,1),max(h,1),frame=frame,strokeColor=color,strokeWidth=sw,roughness=1,
           points=[list(p) for p in pts], lastCommittedPoint=None,
           startBinding=None,endBinding=None,startArrowhead=None,endArrowhead="arrow",roundness={"type":2})
    els.append(e); return e

# ---- SCREEN ----
# NB: deliberately a plain rectangle, NOT an Excalidraw "frame". Frames clip their
# children, and on load Excalidraw auto-captures overlapping elements (annotations,
# arrows) into the frame and clips the parts outside its bounds -> they disappear.
# A rectangle never clips, so every annotation/arrow renders in full.
FW, FH = 1180, 884
rect(0, 0, FW, FH, bg="#fbfbfc", stroke="#bdbdbd", sw=2, rough=0)

# Title above frame
text(0,-46,"MVP 1 — Overview  (v2)",fs=26,color="#1e1e1e",frame=False)

# ---- HEADER NAV ----
rect(20,20,FW-40,56, bg="#ffffff", stroke="#e4e4e7")
ellipse(46,48,18,"#1971c2")
text(70,38,"DB Scheduler UI",fs=18,color="#1e1e1e")
text(300,42,"Overview",fs=15,color="#1971c2")
line(298,66,[[0,0],[78,0]],color="#1971c2",sw=2)   # active underline
text(430,42,"Scheduled",fs=15,color="#71717a")
text(560,42,"History",fs=15,color="#71717a")

# ---- PAGE HEADING ----
text(20,100,"All tasks",fs=26,color="#1e1e1e")

# ---- COLUMN HEADERS ----
CH_Y=158
text(70,CH_Y,"TASK",fs=11,color="#a1a1aa")
text(622,CH_Y,"NEXT RUN",fs=11,color="#a1a1aa")
text(832,CH_Y,"LAST RUN",fs=11,color="#a1a1aa")

CARD_X, CARD_W, CARD_H, GAP = 28, 1120, 60, 8
NEXT_X, LAST_X, LINK_X = 622, 832, 1052
RED="#c92a2a"; BLUE="#1971c2"; GREEN="#2f9e44"; GREY="#71717a"; MUTE="#adb5bd"; ORANGE="#e8590c"
DOT_RED="#c92a2a"; DOT_BLUE="#1971c2"; DOT_GREY="#ced4da"

def row(y, dot, name, sub_segments, nxt, nxt_c, last, last_c, link, link_c=BLUE, tint="#ffffff"):
    rect(CARD_X,y,CARD_W,CARD_H, bg=tint, stroke="#e4e4e7")
    ellipse(CARD_X+18, y+CARD_H/2, 12, dot)
    text(CARD_X+42, y+11, name, fs=15, color="#1e1e1e")
    # subline can be a list of (text,color)
    sx=CARD_X+42; sy=y+33
    for seg,col in sub_segments:
        t=text(sx,sy,seg,fs=12,color=col)
        sx += t["width"]+3
    text(NEXT_X, y+21, nxt, fs=13, color=nxt_c)
    text(LAST_X, y+21, last, fs=12, color=last_c)
    if link: text(LINK_X, y+21, link, fs=12, color=link_c)

# ---- RECURRING SECTION ----
y=188
text(CARD_X,y,"RECURRING · 5",fs=12,color="#71717a")
y+=24
row(y, DOT_GREY, "cleanup-recurring-task",
    [("dormant · 0 instances",GREY)], "—",MUTE, "last success 3d ago",GREEN, "—",MUTE)
y+=CARD_H+GAP
row(y, DOT_RED, "failing-recurring-task",
    [("failing · 2 consecutive failures",RED)], "in 5m","#1e1e1e", "last failure 2m ago",RED, "→ instance", tint="#fff5f5")
y+=CARD_H+GAP
row(y, DOT_GREY, "hourly-report-task",
    [("scheduled · 1 instance",GREY)], "in 26m","#1e1e1e", "last success 34m ago",GREEN, "→ instance")
y+=CARD_H+GAP
row(y, DOT_BLUE, "long-running-recurring-task",
    [("running",BLUE)], "running now",BLUE, "last success 3d ago",GREEN, "→ instance", tint="#f0f7ff")
y+=CARD_H+GAP
row(y, DOT_GREY, "recurring-task",
    [("scheduled · 1 instance",GREY)], "in 1h","#1e1e1e", "last success 2m ago",GREEN, "→ instance")
y+=CARD_H+GAP

# ---- ONE-TIME / CUSTOM SECTION ----
y+=12
text(CARD_X,y,"ONE-TIME / CUSTOM · 4",fs=12,color="#71717a")
y+=24
row(y, DOT_GREY, "chained-step-2",
    [("scheduled · 1 instance",GREY)], "in 6s","#1e1e1e", "last success 2m ago",GREEN, "→ instance")
y+=CARD_H+GAP
row(y, DOT_RED, "failing-one-time-task",
    [("failing · 1 instance",RED)], "due 3h ago",ORANGE, "last failure 2m ago",RED, "→ instance", tint="#fff5f5")
y+=CARD_H+GAP
row(y, DOT_GREY, "import-job",
    [("dormant · 0 instances",GREY)], "—",MUTE, "never run",MUTE, "—",MUTE)
y+=CARD_H+GAP
row(y, DOT_RED, "onetime-spawned-task",
    [("100 instances · ",GREY),("3 failing",RED),(" · ",GREY),("2 running",BLUE),(" · ",GREY),("95 scheduled",GREY)],
    "soonest in 12s","#1e1e1e", "last failure 25 May 09:14",RED, "→ list", tint="#fff5f5")
y+=CARD_H+GAP

# ============ ANNOTATIONS (right margin, red) ============
AX=1230
def note(x,y,s,fs=13,color="#e03131"):
    text(x,y,s,fs=fs,color=color,frame=False)

note(300,-6,"new default landing tab",fs=13)
arrow(360,12,[[0,0],[-20,26]])

note(AX,40,"instanceof RecurringTask\n(registered beans); degrade\ngracefully if SchedulerClient-only",fs=12)
note(AX,196,"dot + word label — colour is\nnever the only status signal",fs=12)
arrow(AX-6,206,[[0,0],[-70,30]])
note(AX,300,"dormant recurring w/ no next\nrun = abnormal (didn't reschedule)",fs=12)
arrow(AX-6,250,[[0,0],[-620,8]])  # to cleanup row next "—"
note(AX,360,"running: 'running now', no fake\nduration (start time not exposed)",fs=12)
note(AX,470,"overdue: scheduled time passed\n→ warning colour",fs=12)
note(AX,560,"group: worst-status dot +\nper-state breakdown · → list",fs=12)
note(AX,104,"most-recent success & failure\nacross instances · server-side\naggregation · works w/o history",fs=12)
arrow(AX-6,118,[[0,0],[-340,46]])

# ============ FUTURE NOTES BOX (below frame) ============
FY=FH+40
rect(0,FY,FW,150, bg="#fffaf0", stroke="#f59f00", frame=False, rough=0)
text(16,FY+10,"FUTURE — not in MVP 1",fs=15,color="#e8590c",frame=False)
fl=[
 "• Global summary bar (top): counts — tasks / failing(tasks+instances) / running / total scheduled — all free from the Overview endpoint.",
 "      'last hour: N ok · N failed' throughput needs history=true; render only then. Counts double as click-to-filter shortcuts.",
 "• Quick-filter chips: Has failures · Running now · Recurring only   |   Task-name search box.",
 "• Cleanup: remove or populate the dead lastHeartbeat field (db-scheduler exposes no heartbeat).",
 "• Later: run-duration for running tasks (blocked on core start-time) · 'problems first' sort toggle.",
]
for i,l in enumerate(fl):
    text(16,FY+40+i*21,l,fs=12,color="#5c5f66",frame=False)

doc=dict(type="excalidraw",version=2,source="https://excalidraw.com",
         elements=els, appState={"gridSize":20,"gridStep":5,"gridModeEnabled":False,
         "viewBackgroundColor":"#f4f4f5","lockedMultiSelections":{}}, files={})
json.dump(doc, open("MVP_1_overview_v2.excalidraw","w"), indent=1)
print("elements:",len(els))
