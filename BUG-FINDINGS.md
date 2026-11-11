# Bug Findings: Technician Booking Conflicts

## 1. Resolved: overlapping bookings were accepted

- **Location:** `backend/src/main/java/com/serviceforge/service/TechnicianAvailabilityService.java`, `bookJob(...)`
- **Root cause:** The conflict check compares only whether an existing job's start time exactly equals the requested start time. It does not compare the jobs' time intervals.
- **Impact:** A technician can be booked for two jobs that overlap when their start times differ. For example, an existing 09:00–11:00 job does not prevent a new 10:00–12:00 booking.
- **Expected behavior:** Reject a new booking when its time interval conflicts with an existing booking for the same technician, and report a conflict rather than silently accepting it.
- **Test gap:** `TechnicianAvailabilityServiceTest` checks equal start times but does not cover overlapping intervals with different start times.
- **Resolution:** `bookJob(...)` now rejects interval conflicts, including conflicts caused by the travel-buffer rule; regression coverage was added.

## 2. Resolved: the documented travel buffer was not enforced by booking

- **Location:** `backend/src/main/java/com/serviceforge/service/TechnicianAvailabilityService.java`, `TRAVEL_BUFFER_MINUTES` and `bookJob(...)`
- **Root cause:** The 45-minute travel-buffer constant is declared, but the booking conflict check does not use it.
- **Impact:** Even after checking for direct interval overlap, a booking may be accepted too soon after an existing job to allow the documented travel time.
- **Context:** `pipeline/decisions/feature-1-decisions.md` says each booked job reserves an additional 45-minute travel buffer. A gap of exactly 45 minutes is allowed.
- **Resolution:** `bookJob(...)` now enforces the buffer in either chronological order.

## Approved fix plan

- Replace the exact-start-time comparison in `TechnicianAvailabilityService.bookJob(...)` with an interval conflict check that also enforces the 45-minute travel gap between consecutive jobs, regardless of which job is earlier.
- Allow a new job when the gap is exactly 45 minutes; reject shorter gaps and any direct overlap with the existing HTTP 409 conflict behavior preserved.
- Add regression tests for different-start overlaps, insufficient travel gaps on either side of an existing job, and the exact-gap boundary.
- Update the feature and decision documentation to describe the resolved behavior, then run the focused backend Maven tests.
